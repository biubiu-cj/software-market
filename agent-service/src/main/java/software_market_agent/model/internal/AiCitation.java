package software_market_agent.model.internal;

import lombok.Data;

/**
 * 结论中的引用编号与历史来源的映射。
 * index 对应 conclusion 中的 [index]。
 */
@Data
public class AiCitation {

    private Integer index;

    private Integer postId;

    private Integer commentId;

    private String title;

    private String url;
}
