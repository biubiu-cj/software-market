package software_market.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software_market.config.KafkaTopicConfig;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagIndexKafkaListener {

    private final @Qualifier("ragKafkaTemplate") KafkaTemplate<String, String> ragKafkaTemplate;

    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRagIndexRequested(RagIndexRequestedEvent event) {
        try {
            RagIndexEvent payload = new RagIndexEvent(
                    UUID.randomUUID().toString(),
                    event.getEventType(),
                    event.getPostId(),
                    event.getVersion(),
                    LocalDateTime.now()
            );
            ragKafkaTemplate.send(
                    KafkaTopicConfig.TOPIC_RAG_INDEX,
                    String.valueOf(event.getPostId()),
                    objectMapper.writeValueAsString(payload)
            ).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送 RAG 索引事件失败: postId={}, type={}", event.getPostId(), event.getEventType(), ex);
                } else {
                    log.info("发送 RAG 索引事件成功: postId={}, type={}", event.getPostId(), event.getEventType());
                }
            });
        } catch (Exception e) {
            // 索引失败不能影响已经提交的发帖/评论业务
            log.error("构造 RAG 索引事件失败: postId={}, type={}", event.getPostId(), event.getEventType(), e);
        }
    }
}
