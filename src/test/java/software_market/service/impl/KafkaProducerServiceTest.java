package software_market.service.impl;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software_market.config.KafkaTopicConfig;
import software_market.entity.Notice;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.mapper.NoticeMapper;
import software_market.service.MailService;
import software_market.webSocket.NoticePushService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KafkaProducerServiceTest {

    private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
    private final MailService mailService = mock(MailService.class);
    private final NoticeMapper noticeMapper = mock(NoticeMapper.class);
    private final NoticePushService noticePushService = mock(NoticePushService.class);
    private final KafkaProducerService service =
            new KafkaProducerService(kafkaTemplate, mailService, noticeMapper, noticePushService);

    @BeforeEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void emailEventIsSentOnlyAfterCommit() {
        mockSuccessfulSend();
        TransactionSynchronizationManager.initSynchronization();

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        service.sendEmailEvent(event);

        verifyNoInteractions(kafkaTemplate);
        assertNotNull(event.getEventId());

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();

        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.TOPIC_EMAIL_VERIFICATION), eq("a@b.com"), same(event));
    }

    @Test
    void notificationEventIsSentOnlyAfterCommit() {
        mockSuccessfulSend();
        TransactionSynchronizationManager.initSynchronization();

        NotificationEvent event = new NotificationEvent(2, 1, "pcomment", "评论了你的帖子", 99);
        service.sendNotificationEvent(event);

        verifyNoInteractions(kafkaTemplate);

        TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.TOPIC_NOTIFICATION), eq("2"), same(event));
    }

    @Test
    void emailFallsBackToDirectSendWhenKafkaFails() {
        when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        service.sendEmailEvent(event);

        verify(mailService, timeout(1000)).sendCode("a@b.com", "123456");
    }

    @Test
    void notificationFallsBackToDirectInsertWhenKafkaFails() {
        when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        NotificationEvent event = new NotificationEvent(2, 1, "pcomment", "评论了你的帖子", 99);
        service.sendNotificationEvent(event);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeMapper, timeout(1000)).insert(captor.capture());

        Notice notice = captor.getValue();
        assertEquals(2, notice.getReceiver());
        assertEquals(1, notice.getSender());
        assertEquals("pcomment", notice.getType());
        assertEquals("评论了你的帖子", notice.getNtext());
        assertEquals(99, notice.getTarget());
        verify(noticePushService, timeout(1000)).pushNewNotice(eq(2), any());
    }

    @SuppressWarnings("unchecked")
    private void mockSuccessfulSend() {
        SendResult<String, Object> sendResult = mock(SendResult.class);
        RecordMetadata metadata = mock(RecordMetadata.class);
        when(metadata.topic()).thenReturn("test-topic");
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(1L);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
    }
}