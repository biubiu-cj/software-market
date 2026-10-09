package software_market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software_market.config.KafkaTopicConfig;
import software_market.entity.Notice;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.mapper.NoticeMapper;
import software_market.service.MailService;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final MailService mailService;
    private final NoticeMapper noticeMapper;

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION, groupId = "email-group")
    public void handleEmailEvent(EmailEvent event) {
        log.info("收到邮件事件: to={}", event.getTo());
        mailService.sendCode(event.getTo(), event.getCode());
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_NOTIFICATION, groupId = "notification-group")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("收到通知事件: type={}, receiver={}", event.getType(), event.getReceiver());
        Notice notice = new Notice();
        notice.setReceiver(event.getReceiver());
        notice.setSender(event.getSender());
        notice.setType(event.getType());
        notice.setNtext(event.getText());
        notice.setTime(LocalDateTime.now());
        notice.setIsRead(false);
        notice.setTarget(event.getTarget());
        noticeMapper.insert(notice);
    }
}
