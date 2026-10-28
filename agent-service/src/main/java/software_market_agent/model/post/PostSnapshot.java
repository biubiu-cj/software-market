package software_market_agent.model.post;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostSnapshot {

    private Integer postId;
    private String title;
    private String content;
    private String category;
    private String tag;
    private LocalDateTime createdAt;
    private List<CommentSnapshot> comments = new ArrayList<>();
}
