package software_market.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_EMAIL_VERIFICATION = "email-verification";
    public static final String TOPIC_NOTIFICATION = "notification-events";

    @Bean
    public NewTopic emailVerificationTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_VERIFICATION)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
