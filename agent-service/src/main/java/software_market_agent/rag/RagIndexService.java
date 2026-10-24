package software_market_agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software_market_agent.datasource.RagSnapshotReader;
import software_market_agent.embedding.EmbeddingClient;
import software_market_agent.exception.RagUnavailableException;
import software_market_agent.milvus.VectorStoreClient;
import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.post.PostSnapshot;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagIndexService {

    private final RagSnapshotReader snapshotReader;
    private final RagDocumentService documentService;
    private final EmbeddingClient embeddingClient;
    private final VectorStoreClient vectorStoreClient;

    /**
     * 整帖重建：先构建并完成 embedding，再删除旧向量并写入新向量。
     * 重复消费同一事件会得到相同的文档 ID 和相同结果。
     */
    public void reindex(Integer postId, Long version) {
        if (postId == null) {
            return;
        }
        long docVersion = version == null || version <= 0 ? 1L : version;
        PostSnapshot post = snapshotReader.read(postId);
        if (post == null) {
            vectorStoreClient.deleteByPostId(postId);
            log.info("帖子不存在，已清理向量: postId={}", postId);
            return;
        }

        List<VectorDocument> documents = documentService.build(post, docVersion);
        if (documents.isEmpty()) {
            vectorStoreClient.deleteByPostId(postId);
            log.info("没有可索引内容，已清理向量: postId={}", postId);
            return;
        }

        List<float[]> embeddings = embeddingClient.embedBatch(
                documents.stream().map(VectorDocument::getContent).toList()
        );
        if (embeddings.size() != documents.size()) {
            throw new RagUnavailableException("Embedding 数量与文档数量不一致: docs="
                    + documents.size() + ", embeddings=" + embeddings.size());
        }
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).setEmbedding(embeddings.get(i));
        }

        vectorStoreClient.deleteByPostId(postId);
        vectorStoreClient.upsertBatch(documents);
        log.info("RAG 索引完成: postId={}, version={}, docs={}", postId, docVersion, documents.size());
    }

    public void delete(Integer postId) {
        if (postId == null) {
            return;
        }
        vectorStoreClient.deleteByPostId(postId);
        log.info("RAG 删除完成: postId={}", postId);
    }

    public int backfillAll() {
        List<Integer> postIds = snapshotReader.allPostIds();
        int success = 0;
        for (Integer postId : postIds) {
            try {
                reindex(postId, 1L);
                success++;
            } catch (Exception e) {
                log.error("回填失败: postId={}", postId, e);
            }
        }
        log.info("RAG 全量回填完成: total={}, success={}", postIds.size(), success);
        return success;
    }
}
