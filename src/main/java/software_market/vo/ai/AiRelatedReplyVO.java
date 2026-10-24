package software_market.vo.ai;

import lombok.Data;

@Data
public class AiRelatedReplyVO {

    private Integer postId;

    private Integer commentId;

    private Integer citationIndex;

    private String title;

    private String sourceType;

    private String content;

    private String createdAt;

    private Double score;

    private String url;
}
