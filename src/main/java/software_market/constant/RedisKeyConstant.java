package software_market.constant;

public class RedisKeyConstant {
    public static final String VCODE="vcode:";
    public static final String VCODE_FAIL="vcode_fail:";
    public static final String HOT_POSTS_ZSET="hot_posts_zset";
    /** Kafka 消费幂等标记前缀, 完整键形如 mq:dedup:notice:{eventId} */
    public static final String MQ_DEDUP="mq:dedup:";

    /** AI 分析结果缓存前缀，完整键形如 ai:post-analysis:v2:{postId} */
    public static final String AI_ANALYSIS_CACHE = "ai:post-analysis:v2:";

    /** AI 接口用户限流前缀，完整键形如 ai:rate:{userId} */
    public static final String AI_RATE_LIMIT = "ai:rate:";
}
