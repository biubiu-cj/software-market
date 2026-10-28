package software_market.vo.ai;

import lombok.Data;

@Data
public class AiRelatedPostVO {

    private Integer postId;

    private String title;

    private String category;

    private String summary;

    private String createdAt;

    private Double matchScore;

    private Integer evidenceCount;

    private String url;
}
