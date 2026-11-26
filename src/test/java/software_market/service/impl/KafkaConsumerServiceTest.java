package software_market.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import software_market.entity.Notice;
import software_market.event.EmailEvent;
import software_market.event.NotificationEvent;
import software_market.mapper.NoticeMapper;
import software_market.service.MailService;
import software_market.webSocket.NoticePushService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KafkaConsumerServiceTest {

    private final MailService mailService = mock(MailService.class);
    private final NoticeMapper noticeMapper = mock(NoticeMapper.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final NoticePushService noticePushService = mock(NoticePushService.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final KafkaConsumerService service =
            new KafkaConsumerService(mailService, noticeMapper, redis, noticePushService);

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void emailProcessesOnceAndMarksDone() {
        String key = "mq:dedup:email:evt-1";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(true);

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        event.setEventId("evt-1");
        service.handleEmailEvent(event);

        verify(mailService).sendCode("a@b.com", "123456");
        verify(valueOps).set(key, "DONE", Duration.ofDays(7));
    }

    @Test
    void duplicateEmailIsSkipped() {
        String key = "mq:dedup:email:evt-2";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(false);
        when(valueOps.get(key)).thenReturn("DONE");

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        event.setEventId("evt-2");
        service.handleEmailEvent(event);

        verifyNoInteractions(mailService);
        verify(valueOps, never()).set(eq(key), eq("DONE"), any(Duration.class));
    }

    @Test
    void legacyProcessedMarkerIsTreatedAsDone() {
        String key = "mq:dedup:email:evt-legacy";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(false);
        when(valueOps.get(key)).thenReturn("1");

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        event.setEventId("evt-legacy");
        service.handleEmailEvent(event);

        verifyNoInteractions(mailService);
    }

    @Test
    void eventBeingProcessedByAnotherConsumerTriggersRetry() {
        String key = "mq:dedup:email:evt-3";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(false);
        when(valueOps.get(key)).thenReturn("PROCESSING");

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        event.setEventId("evt-3");

        assertThrows(IllegalStateException.class, () -> service.handleEmailEvent(event));
        verifyNoInteractions(mailService);
    }

    @Test
    void emailFailureReleasesClaimForRetry() {
        String key = "mq:dedup:email:evt-4";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(true);
        doThrow(new RuntimeException("smtp down")).when(mailService).sendCode(anyString(), anyString());

        EmailEvent event = new EmailEvent("a@b.com", "123456");
        event.setEventId("evt-4");

        assertThrows(RuntimeException.class, () -> service.handleEmailEvent(event));
        verify(redis).delete(key);
    }

    @Test
    void notificationInsertsNoticeAndMarksDone() {
        String key = "mq:dedup:notice:evt-5";
        when(valueOps.setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))).thenReturn(true);

        NotificationEvent event = new NotificationEvent(2, 1, "ccomment", "回复了你的评论", 99);
        event.setEventId("evt-5");
        service.handleNotificationEvent(event);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getReceiver());
        assertEquals("ccomment", captor.getValue().getType());
        verify(noticePushService).pushNewNotice(eq(2), any());
        verify(valueOps).set(key, "DONE", Duration.ofDays(7));
    }

    @Test
    void eventWithoutEventIdSkipsDedup() {
        EmailEvent event = new EmailEvent("a@b.com", "123456");

        service.handleEmailEvent(event);

        verify(mailService).sendCode("a@b.com", "123456");
        verifyNoInteractions(redis);
    }
}