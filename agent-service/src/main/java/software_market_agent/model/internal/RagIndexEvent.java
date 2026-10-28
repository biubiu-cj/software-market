package software_market_agent.model.internal;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagIndexEvent {

    private String eventId;
    private String eventType;
    private Integer postId;
    private Long version;
    private LocalDateTime occurredAt;
}
