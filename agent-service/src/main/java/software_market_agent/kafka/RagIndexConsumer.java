package software_market_agent.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software_market_agent.model.internal.RagIndexEvent;
import software_market_agent.rag.RagIndexService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagIndexConsumer {

    private final ObjectMapper objectMapper;

    private final RagIndexService ragIndexService;

    @KafkaListener(topics = "rag-index-topic", groupId = "agent-rag-indexer")
    public void consume(String payload) {
        RagIndexEvent event;
        try {
            event = objectMapper.readValue(payload, RagIndexEvent.class);
        } catch (Exception e) {
            // 非法 JSON 属于毒消息，重试没有意义
            log.error("RAG 索引事件解析失败，跳过: payload={}", payload, e);
            return;
        }
        if (event.getPostId() == null) {
            log.warn("RAG 索引事件缺少 postId，跳过: eventId={}", event.getEventId());
            return;
        }
        log.info("收到 RAG 索引事件: eventId={}, type={}, postId={}, version={}",
                event.getEventId(), event.getEventType(), event.getPostId(), event.getVersion());
        try {
            if ("POST_DELETE".equals(event.getEventType())) {
                ragIndexService.delete(event.getPostId());
            } else {
                ragIndexService.reindex(event.getPostId(), event.getVersion());
            }
        } catch (Exception e) {
            log.error("处理 RAG 索引事件失败，将交给重试/DLT: eventId={}, postId={}",
                    event.getEventId(), event.getPostId(), e);
            throw e;
        }
    }
}
