package software_market.service.impl;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software_market.config.KafkaTopicConfig;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.service.MailService;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MailService mailService;

    /** 降级发信是阻塞 IO, 放在独立线程, 避免卡住 Kafka 生产者的 IO 线程 */
    private final ExecutorService fallbackExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "kafka-email-fallback");
        t.setDaemon(true);
        return t;
    });

    public void sendEmailEvent(EmailEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        log.info("准备发送邮件事件: to={}, eventId={}", event.getTo(), event.getEventId());

        kafkaTemplate.send(KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, event.getTo(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送邮件事件失败, 降级为直接发信: to={}, eventId={}",
                                event.getTo(), event.getEventId(), ex);
                        fallbackExecutor.execute(() -> sendEmailDirect(event));
                    } else {
                        log.info("发送邮件事件成功: to={}, topic={}, partition={}, offset={}",
                                event.getTo(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /** Kafka 不可用时的兜底, 保证验证码邮件至少尝试直接发送一次 */
    private void sendEmailDirect(EmailEvent event) {
        try {
            mailService.sendCode(event.getTo(), event.getCode());
            log.info("邮件降级发送成功: to={}, eventId={}", event.getTo(), event.getEventId());
        } catch (Exception e) {
            log.error("邮件降级发送失败: to={}, eventId={}", event.getTo(), event.getEventId(), e);
        }
    }

    public void sendNotificationEvent(NotificationEvent event) {
        event.setEventId(UUID.randomUUID().toString());

        // 以 receiver 为 key: 同一用户的通知落在同一分区, 保证顺序
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATION,
                        String.valueOf(event.getReceiver()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送通知事件失败: type={}, receiver={}, eventId={}",
                                event.getType(), event.getReceiver(), event.getEventId(), ex);
                    }
                });
    }

    @PreDestroy
    public void shutdown() {
        fallbackExecutor.shutdown();
    }
}
