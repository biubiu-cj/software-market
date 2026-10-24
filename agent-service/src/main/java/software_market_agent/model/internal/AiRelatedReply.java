package software_market_agent.model.internal;

import lombok.Data;

@Data
public class AiRelatedReply {

    private Integer postId;
    private Integer commentId;
    /** 对应 conclusion 中的 [citationIndex] */
    private Integer citationIndex;
    private String title;
    private String sourceType;
    private String content;
    private String createdAt;
    private Double score;
    private String url;
}
