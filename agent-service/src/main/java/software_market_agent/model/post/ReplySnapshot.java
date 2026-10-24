package software_market_agent.model.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplySnapshot {

    private Integer replyId;
    private String content;
    private String targetName;
    private LocalDateTime createdAt;
}
