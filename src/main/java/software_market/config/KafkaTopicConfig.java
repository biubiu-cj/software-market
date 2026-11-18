package software_market.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Slf4j
@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_EMAIL_VERIFICATION = "email-verification";
    public static final String TOPIC_NOTIFICATION = "notification-events";
    public static final String TOPIC_RAG_INDEX = "rag-index-topic";

    public static final String TOPIC_EMAIL_VERIFICATION_DLT = TOPIC_EMAIL_VERIFICATION + ".DLT";
    public static final String TOPIC_NOTIFICATION_DLT = TOPIC_NOTIFICATION + ".DLT";
    public static final String TOPIC_RAG_INDEX_DLT = TOPIC_RAG_INDEX + ".DLT";

    /** 邮件/通知 topic 副本数；单 broker 默认 1，多 broker 环境通过 KAFKA_TOPIC_REPLICAS 提高 */
    @Value("${app.kafka.topic-replicas:1}")
    private int topicReplicas;

    @PostConstruct
    void warnIfSingleReplica() {
        if (topicReplicas < 2) {
            log.warn("Kafka 邮件/通知 topic replicas={}，当前没有副本冗余；多 broker 环境请设置 KAFKA_TOPIC_REPLICAS>=2",
                    topicReplicas);
        }
    }

    @Bean
    public NewTopic emailVerificationTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_VERIFICATION)
                .partitions(1)
                .replicas(topicReplicas)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION)
                .partitions(2)
                .replicas(topicReplicas)
                .build();
    }

    @Bean
    public NewTopic emailVerificationDltTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_VERIFICATION_DLT)
                .partitions(1)
                .replicas(topicReplicas)
                .build();
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION_DLT)
                .partitions(2)
                .replicas(topicReplicas)
                .build();
    }

    @Bean
    public NewTopic ragIndexTopic() {
        return TopicBuilder.name(TOPIC_RAG_INDEX)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ragIndexDltTopic() {
        return TopicBuilder.name(TOPIC_RAG_INDEX_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 消费失败指数退避重试 5 次, 仍失败则投递到 "{topic}.DLT", 避免消息被静默丢弃。
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30_000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
