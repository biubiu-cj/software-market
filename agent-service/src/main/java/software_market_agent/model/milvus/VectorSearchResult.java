package software_market_agent.model.milvus;

import lombok.Data;

@Data
public class VectorSearchResult {

    private String id;
    private double score;
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
