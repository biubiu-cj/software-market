package software_market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software_market.config.KafkaTopicConfig;
import software_market.constant.RedisKeyConstant;
import software_market.entity.Notice;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.mapper.NoticeMapper;
import software_market.service.MailService;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    /** 需覆盖 Kafka 消息保留期, 否则过期后重放仍会重复处理 */
    private static final Duration DEDUP_TTL = Duration.ofDays(7);

    private final MailService mailService;
    private final NoticeMapper noticeMapper;
    private final StringRedisTemplate redis;

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, groupId = "email-group",
            properties = {"auto.offset.reset=latest"})
    public void handleEmailEvent(EmailEvent event) {
        String dedupKey = dedupKey("email", event.getEventId());
        if (isProcessed(dedupKey)) {
            log.info("重复邮件事件, 跳过: to={}, eventId={}", event.getTo(), event.getEventId());
            return;
        }
        log.info("收到邮件事件: to={}, eventId={}", event.getTo(), event.getEventId());
        mailService.sendCode(event.getTo(), event.getCode());
        markProcessed(dedupKey);
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_NOTIFICATION, groupId = "notification-group",
            concurrency = "2")
    public void handleNotificationEvent(NotificationEvent event) {
        String dedupKey = dedupKey("notice", event.getEventId());
        if (isProcessed(dedupKey)) {
            log.info("重复通知事件, 跳过: type={}, receiver={}, eventId={}",
                    event.getType(), event.getReceiver(), event.getEventId());
            return;
        }
        log.info("收到通知事件: type={}, receiver={}, eventId={}",
                event.getType(), event.getReceiver(), event.getEventId());

        Notice notice = new Notice();
        notice.setReceiver(event.getReceiver());
        notice.setSender(event.getSender());
        notice.setType(event.getType());
        notice.setNtext(event.getText());
        notice.setTime(LocalDateTime.now());
        notice.setIsRead(false);
        notice.setTarget(event.getTarget());
        noticeMapper.insert(notice);

        markProcessed(dedupKey);
    }

    /**
     * 兼容升级前已在队列中、没有 eventId 的旧消息: 这类消息不做去重。
     */
    private String dedupKey(String type, String eventId) {
        return eventId == null ? null : RedisKeyConstant.MQ_DEDUP + type + ":" + eventId;
    }

    private boolean isProcessed(String key) {
        return key != null && Boolean.TRUE.equals(redis.hasKey(key));
    }

    private void markProcessed(String key) {
        if (key != null) {
            redis.opsForValue().set(key, "1", DEDUP_TTL);
        }
    }
}
