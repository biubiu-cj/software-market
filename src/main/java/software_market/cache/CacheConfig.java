package software_market.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class CacheConfig {

    public static final String CACHE_EVICT_TOPIC = "cache:evict";

    /**
     * 跨实例 L1 缓存同步 — 订阅缓存驱逐频道
     */
    @Bean
    public RedisMessageListenerContainer cacheEvictContainer(
            RedisConnectionFactory factory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(CACHE_EVICT_TOPIC));
        return container;
    }

    @Bean
    public MessageListenerAdapter cacheEvictListener(MultiLevelCache multiLevelCache) {
        return new MessageListenerAdapter(multiLevelCache, "handleCacheEvict");
    }
}
