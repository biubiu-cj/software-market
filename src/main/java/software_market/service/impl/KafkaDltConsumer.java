package software_market.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software_market.config.KafkaTopicConfig;

import java.nio.charset.StandardCharsets;

/**
 * 死信队列消费者：DLT 消息只做 ERROR 日志告警，供日志平台/人工排查。
 */
@Slf4j
@Component
public class KafkaDltConsumer {

    private static final int MAX_PAYLOAD_LENGTH = 1_000;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION_DLT,
            groupId = "email-dlt-group",
            containerFactory = "dltListenerContainerFactory")
    public void handleEmailDlt(ConsumerRecord<String, byte[]> record) {
        logDlt("email", record);
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_NOTIFICATION_DLT,
            groupId = "notification-dlt-group",
            containerFactory = "dltListenerContainerFactory")
    public void handleNotificationDlt(ConsumerRecord<String, byte[]> record) {
        logDlt("notification", record);
    }

    private void logDlt(String type, ConsumerRecord<String, byte[]> record) {
        String payload = record.value() == null
                ? ""
                : new String(record.value(), StandardCharsets.UTF_8);
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            payload = payload.substring(0, MAX_PAYLOAD_LENGTH) + "...";
        }
        log.error("Kafka 死信告警 type={} topic={} partition={} offset={} key={} payload={}",
                type, record.topic(), record.partition(), record.offset(), record.key(), payload);
    }
}