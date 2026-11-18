package software_market.service.impl;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software_market.config.KafkaTopicConfig;
import software_market.entity.Notice;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.mapper.NoticeMapper;
import software_market.service.MailService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Kafka 事件生产端。
 *
 * 事件统一在数据库事务提交后发送，避免“事务回滚但邮件/通知已经发出”。
 * 注意：KafkaTemplate.send 本身是异步的，无法覆盖“事务已提交但 JVM 在 broker
 * ack 前崩溃”的窗口；要彻底保证需要 outbox 表 + 定时投递。这里的降级只覆盖
 * send 失败（邮件直接发信、通知直接写 notice 表）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MailService mailService;
    private final NoticeMapper noticeMapper;

    /** 降级发信/写库是阻塞 IO, 放在独立线程, 避免卡住 Kafka 生产者的 IO 线程 */
    private final ExecutorService fallbackExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "kafka-event-fallback");
        t.setDaemon(true);
        return t;
    });

    public void sendEmailEvent(EmailEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        afterCommit(() -> doSendEmailEvent(event));
    }

    private void doSendEmailEvent(EmailEvent event) {
        log.info("准备发送邮件事件: to={}, eventId={}", event.getTo(), event.getEventId());
        try {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, event.getTo(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送邮件事件失败, 降级为直接发信: to={}, eventId={}",
                                    event.getTo(), event.getEventId(), ex);
                            executeFallback(() -> sendEmailDirect(event));
                        } else {
                            log.info("发送邮件事件成功: to={}, topic={}, partition={}, offset={}",
                                    event.getTo(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            // send 同步抛异常（例如序列化失败）时也要走降级
            log.error("发送邮件事件异常, 降级为直接发信: to={}, eventId={}",
                    event.getTo(), event.getEventId(), e);
            executeFallback(() -> sendEmailDirect(event));
        }
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
        afterCommit(() -> doSendNotificationEvent(event));
    }

    private void doSendNotificationEvent(NotificationEvent event) {
        try {
            // 以 receiver 为 key: 同一用户的通知落在同一分区, 保证顺序
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATION,
                            String.valueOf(event.getReceiver()), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送通知事件失败, 降级写库: type={}, receiver={}, eventId={}",
                                    event.getType(), event.getReceiver(), event.getEventId(), ex);
                            executeFallback(() -> saveNoticeDirect(event));
                        } else {
                            log.info("发送通知事件成功: type={}, receiver={}, partition={}, offset={}",
                                    event.getType(), event.getReceiver(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("发送通知事件异常, 降级写库: type={}, receiver={}, eventId={}",
                    event.getType(), event.getReceiver(), event.getEventId(), e);
            executeFallback(() -> saveNoticeDirect(event));
        }
    }

    /** Kafka 不可用时的兜底: 直接把通知写入 notice 表, 避免通知静默丢失 */
    private void saveNoticeDirect(NotificationEvent event) {
        try {
            Notice notice = new Notice();
            notice.setReceiver(event.getReceiver());
            notice.setSender(event.getSender());
            notice.setType(event.getType());
            notice.setNtext(event.getText());
            notice.setTime(LocalDateTime.now());
            notice.setIsRead(false);
            notice.setTarget(event.getTarget());
            noticeMapper.insert(notice);
            log.info("通知降级写库成功: type={}, receiver={}, eventId={}",
                    event.getType(), event.getReceiver(), event.getEventId());
        } catch (Exception e) {
            log.error("通知降级写库失败: type={}, receiver={}, eventId={}",
                    event.getType(), event.getReceiver(), event.getEventId(), e);
        }
    }

    /**
     * 有事务时注册 afterCommit 回调；没有事务时（例如异步线程）直接执行。
     */
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        runQuietly(action);
                    }
                });
            } catch (Exception e) {
                log.error("注册事务提交回调失败, 事件不会发送", e);
            }
        } else {
            runQuietly(action);
        }
    }

    private void runQuietly(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("事务提交后发送 Kafka 事件失败", e);
        }
    }

    private void executeFallback(Runnable task) {
        try {
            fallbackExecutor.execute(task);
        } catch (Exception e) {
            log.error("提交 Kafka 降级任务失败", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            // 优雅停机时尽量把生产者缓冲区里的消息刷出去，降低停机丢消息概率
            kafkaTemplate.flush();
        } catch (Exception e) {
            log.warn("Kafka 生产者 flush 失败", e);
        }
        fallbackExecutor.shutdown();
    }
}