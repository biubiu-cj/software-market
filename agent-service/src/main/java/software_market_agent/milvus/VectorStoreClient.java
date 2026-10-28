package software_market_agent.milvus;

import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.List;

public interface VectorStoreClient {

    void upsertBatch(List<VectorDocument> documents);

    void deleteByPostId(Integer postId);

    List<VectorSearchResult> search(
            float[] queryVector,
            int topK,
            double threshold,
            Integer excludePostId
    );
}
