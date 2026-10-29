package software_market_agent.model.milvus;

import lombok.Data;

@Data
public class VectorSearchResult {

    private String id;
    private double score;
    /** RRF 融合分；仅用于多路召回排序，不代表余弦相似度 */
    private double rrfScore;
    /** 命中该片段的检索路径：ORIGINAL / REWRITTEN / HYBRID */
    private String retrievalSource;
    private long postId;
    private long commentId;
    private String title;
    private String content;
    private String category;
    private String tag;
    private String sourceType;
    private long createdAt;
    private String status;
    private long docVersion;
    private long chunkIndex;
}
