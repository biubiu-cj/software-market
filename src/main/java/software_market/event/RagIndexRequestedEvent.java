package software_market.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 主应用内部事务事件，事务提交后再转换成 Kafka 消息。
 */
@Data
@AllArgsConstructor
public class RagIndexRequestedEvent {

    private Integer postId;

    private String eventType;

    private Long version;
}
