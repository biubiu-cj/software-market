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
import software_market.webSocket.NoticePushService;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    /** 处理完成标记的 TTL, 需覆盖 Kafka 消息保留期, 否则过期后重放仍会重复处理 */
    private static final Duration DEDUP_TTL = Duration.ofDays(7);

    /** 处理中占位 TTL：防止并发消费者重复处理，同时在消费者崩溃后允许最终重试 */
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(5);

    private static final String STATE_PROCESSING = "PROCESSING";
    private static final String STATE_DONE = "DONE";

    private final MailService mailService;
    private final NoticeMapper noticeMapper;
    private final StringRedisTemplate redis;
    private final NoticePushService noticePushService;

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, groupId = "email-group",
            properties = {"auto.offset.reset=latest"})
    public void handleEmailEvent(EmailEvent event) {
        String dedupKey = dedupKey("email", event.getEventId());
        if (!claim(dedupKey)) {
            log.info("重复邮件事件, 跳过: to={}, eventId={}", event.getTo(), event.getEventId());
            return;
        }

        try {
            log.info("收到邮件事件: to={}, eventId={}", event.getTo(), event.getEventId());
            mailService.sendCode(event.getTo(), event.getCode());
            markDone(dedupKey);
        } catch (Exception e) {
            release(dedupKey);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_NOTIFICATION, groupId = "notification-group",
            concurrency = "2")
    public void handleNotificationEvent(NotificationEvent event) {
        String dedupKey = dedupKey("notice", event.getEventId());
        if (!claim(dedupKey)) {
            log.info("重复通知事件, 跳过: type={}, receiver={}, eventId={}",
                    event.getType(), event.getReceiver(), event.getEventId());
            return;
        }

        try {
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

            // 通知已落库，推送只是加速可见；失败由 NoticePushService 内部吞掉，不触发重试
            noticePushService.pushNewNotice(event.getReceiver(), notice.getNoticeId());

            markDone(dedupKey);
        } catch (Exception e) {
            release(dedupKey);
            throw e;
        }
    }

    /**
     * 原子占位：
     * - 第一次到达：SETNX 成功，进入业务处理；
     * - 已处理完成：返回 false，直接跳过；
     * - 正在被另一个消费者处理：抛异常让 Kafka 重试，而不是静默跳过。
     */
    private boolean claim(String key) {
        if (key == null) {
            return true;
        }
        Boolean acquired = redis.opsForValue().setIfAbsent(key, STATE_PROCESSING, PROCESSING_TTL);
        if (Boolean.TRUE.equals(acquired)) {
            return true;
        }

        String state = redis.opsForValue().get(key);
        // DONE 或旧版本的 "1" 标记都表示已处理完成，直接跳过
        if (state != null && !STATE_PROCESSING.equals(state)) {
            return false;
        }
        log.warn("事件正在被其他消费者处理, 触发重试: key={}, state={}", key, state);
        throw new IllegalStateException("事件正在处理中: " + key);
    }

    private void markDone(String key) {
        if (key != null) {
            redis.opsForValue().set(key, STATE_DONE, DEDUP_TTL);
        }
    }

    /** 业务失败时释放占位，让 Kafka 重试时还能重新获取处理权 */
    private void release(String key) {
        if (key == null) {
            return;
        }
        try {
            redis.delete(key);
        } catch (Exception e) {
            log.warn("释放去重占位失败: key={}", key, e);
        }
    }

    /**
     * 兼容升级前已在队列中、没有 eventId 的旧消息: 这类消息不做去重。
     */
    private String dedupKey(String type, String eventId) {
        return eventId == null ? null : RedisKeyConstant.MQ_DEDUP + type + ":" + eventId;
    }
}