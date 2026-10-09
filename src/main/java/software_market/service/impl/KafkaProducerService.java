package software_market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software_market.config.KafkaTopicConfig;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(EmailEvent event) {
        log.info("准备发送邮件事件: to={}", event.getTo());

        kafkaTemplate.send(KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送邮件事件失败: to={}", event.getTo(), ex);
                    } else {
                        log.info("发送邮件事件成功: to={}, topic={}, partition={}, offset={}",
                                event.getTo(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void sendNotificationEvent(NotificationEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATION, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送通知事件失败: type={}, receiver={}", event.getType(), event.getReceiver(), ex);
                    }
                });
    }
}
