package software_market_agent.model.internal;

import lombok.Data;

@Data
public class AiRelatedPost {

    private Integer postId;
    private String title;
    private String category;
    private String summary;
    private String createdAt;
    private Double matchScore;
    private Integer evidenceCount;
    private String url;
}
