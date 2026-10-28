package software_market_agent.model.post;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentSnapshot {

    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;
    private List<ReplySnapshot> replies = new ArrayList<>();
}
