package software_market.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 发送给 agent-service 的 RAG 索引事件。
 * 使用 JSON 字符串传输，避免跨进程共享 Java 类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagIndexEvent {

    private String eventId;

    private String eventType;

    private Integer postId;

    private Long version;

    private LocalDateTime occurredAt;
}
