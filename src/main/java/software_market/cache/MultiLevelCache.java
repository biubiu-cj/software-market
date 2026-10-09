package software_market.cache;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * L1(Caffeine) + L2(Redis) 多级缓存, 内置缓存穿透/击穿/雪崩防护.
 *
 * <pre>
 * 读取流程:
 *   L1 命中   → 直接返回 (纳秒级, 不碰 Redis)
 *   L1 未命中 → 交给 Caffeine 的单飞加载函数: L2 命中则回填 L1, L2 未命中则查 DB 并回填 L2+L1
 *
 * 防护:
 *   - 穿透: 查不到时写入空值标记 "__NULL__", L1/L2 均为 30s 短 TTL
 *   - 击穿: 单飞(single-flight)。Caffeine 的 get(key, loader) 保证同一 key 的加载函数
 *           在进程内至多被一个线程执行, 其余线程阻塞等待并复用结果, 因此不会有
 *           并发流量同时穿透到 DB。
 *   - 雪崩: L2 TTL 附加 0~20% 随机抖动, 避免大批 key 同时失效
 *
 * 多实例一致性:
 *   evict() 通过 Redis Pub/Sub 广播被驱逐的 key, 其他实例收到后清掉各自的 L1,
 *   把跨实例的不一致窗口从 L1 的 TTL(120s) 缩短到消息投递延迟。
 *
 * 为什么没有用 Redis 分布式锁:
 *   本服务当前是单实例部署, 进程内的单飞已经能完全防住击穿; 分布式锁只会额外引入
 *   锁超时与误删他人锁的一致性问题。水平扩容后, 最坏情况是每个实例各查一次 DB,
 *   相对 DB 容量可以接受; 若届时确需全局互斥, 再补 SETNX+Lua 才有意义。
 * </pre>
 */
@Slf4j
@Component
public class MultiLevelCache {

    private final Cache<String, String> l1;
    private final StringRedisTemplate redis;

    private static final int L1_TTL_SECONDS = 120;
    private static final int L1_MAX_SIZE = 2000;

    /** 空值标记的 TTL 必须远短于正常值, 否则"缓存穿透期间写入的空值"会把后续真正出现的数据长期压住 */
    private static final int NULL_L1_TTL_SECONDS = 30;
    private static final int NULL_L2_TTL_SECONDS = 30;

    private static final String NULL_MARKER = "__NULL__";

    public MultiLevelCache(StringRedisTemplate redis) {
        this.redis = redis;
        this.l1 = Caffeine.newBuilder()
                .maximumSize(L1_MAX_SIZE)
                .expireAfter(new PerEntryTtl())
                .build();
        log.info("MultiLevelCache 初始化完成 — L1(Caffeine maxSize={}, ttl={}s) + L2(Redis)",
                L1_MAX_SIZE, L1_TTL_SECONDS);
    }

    // ==================== 读取 ====================

    /**
     * L1 → L2 → DB 三级读取, 内置穿透/击穿/雪崩防护。
     *
     * @param key          缓存键 (命名规范: "{domain}:{id}", 如 "post:123")
     * @param clazz        目标类型
     * @param l2TtlSeconds L2(Redis) 过期秒数, 实际会附加随机抖动
     * @param dbLoader     DB 查询回调 — 缓存未命中时执行
     */
    public <T> T get(String key, Class<T> clazz, int l2TtlSeconds, Supplier<T> dbLoader) {
        return get(key, (Type) clazz, l2TtlSeconds, dbLoader);
    }

    /**
     * 用于泛型类型 (如 {@code List<Tag>}) 的读取。
     *
     * @param key          缓存键
     * @param type         目标泛型类型, 使用 {@code new TypeReference<List<Foo>>() {}.getType()}
     * @param l2TtlSeconds L2 TTL 秒数
     * @param dbLoader     DB 查询回调
     */
    public <T> T get(String key, Type type, int l2TtlSeconds, Supplier<T> dbLoader) {
        // Caffeine 的原子加载: 同一 key 并发未命中时只有一个线程执行 loader, 其余阻塞等待
        String cached = l1.get(key, k -> load(k, l2TtlSeconds, dbLoader));
        return nullMarkerToNull(cached, type);
    }

    /**
     * 单飞加载函数 — 只在 L1 未命中时被调用, 且同一 key 同时至多一个线程进入。
     * 因此这里无需再做双重检查。
     *
     * @return 永不为 null 的字符串; 查不到时返回 {@link #NULL_MARKER}
     */
    private String load(String key, int l2TtlSeconds, Supplier<?> dbLoader) {
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            log.debug("L2 命中 key={}", key);
            return cached;
        }

        Object data = dbLoader.get();
        String value = data == null ? NULL_MARKER : JSON.toJSONString(data);
        int ttl = data == null ? NULL_L2_TTL_SECONDS : withJitter(l2TtlSeconds);
        redis.opsForValue().set(key, value, Duration.ofSeconds(ttl));
        log.debug("回填 L1+L2 key={} ttl={}s 空值={}", key, ttl, data == null);
        return value;
    }

    // ==================== 驱逐 ====================

    /** 驱逐 L1 + L2, 并广播通知其他实例驱逐各自 L1 */
    public void evict(String key) {
        l1.invalidate(key);
        redis.delete(key);
        redis.convertAndSend(CacheConfig.CACHE_EVICT_TOPIC, key);
    }

    /** 跨实例驱逐回调 — 由 {@link CacheConfig} 的 MessageListenerAdapter 按方法名反射调用, 勿改名 */
    @SuppressWarnings("unused")
    public void handleCacheEvict(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        l1.invalidate(key);
        log.debug("跨实例 L1 驱逐 key={}", key);
    }

    // ==================== 内部实现 ====================

    /** 给 L2 TTL 附加 0~20% 抖动防雪崩; bound 至少为 1, 避免 TTL 很小时 nextInt(0) 抛异常 */
    private static int withJitter(int baseTtlSeconds) {
        int bound = Math.max(1, baseTtlSeconds / 5);
        return baseTtlSeconds + ThreadLocalRandom.current().nextInt(bound);
    }

    @SuppressWarnings("unchecked")
    private <T> T nullMarkerToNull(String json, Type type) {
        if (NULL_MARKER.equals(json)) {
            return null;
        }
        return (T) JSON.parseObject(json, type);
    }

    /** 按 entry 内容决定 L1 存活时长 — 空值标记走短 TTL, Caffeine 的全局 expireAfterWrite 做不到这点 */
    private static final class PerEntryTtl implements Expiry<String, String> {

        @Override
        public long expireAfterCreate(String key, String value, long currentTime) {
            return ttlNanos(value);
        }

        @Override
        public long expireAfterUpdate(String key, String value, long currentTime, long currentDuration) {
            return ttlNanos(value);
        }

        @Override
        public long expireAfterRead(String key, String value, long currentTime, long currentDuration) {
            return currentDuration;
        }

        private static long ttlNanos(String value) {
            int seconds = NULL_MARKER.equals(value) ? NULL_L1_TTL_SECONDS : L1_TTL_SECONDS;
            return Duration.ofSeconds(seconds).toNanos();
        }
    }
}
