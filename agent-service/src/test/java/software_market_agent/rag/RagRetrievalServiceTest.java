package software_market_agent.rag;

import org.junit.jupiter.api.Test;
import software_market_agent.config.AgentProperties;
import software_market_agent.embedding.EmbeddingClient;
import software_market_agent.exception.RagUnavailableException;
import software_market_agent.milvus.VectorStoreClient;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagRetrievalServiceTest {

    @Test
    void excludesCurrentPostAndAggregatesByPostId() {
        AgentProperties properties = properties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);

        when(embeddingClient.embed(any())).thenReturn(new float[1024]);
        when(vectorStoreClient.search(any(float[].class), anyInt(), anyDouble(), eq(999)))
                .thenReturn(List.of(
                        hit(999, 1, 0.99, "当前帖子不应出现"),
                        hit(100, 11, 0.90, "历史帖子一百第一条"),
                        hit(100, 12, 0.80, "历史帖子一百第二条"),
                        hit(100, 13, 0.70, "历史帖子一百第三条"),
                        hit(200, 21, 0.85, "历史帖子二百第一条"),
                        hit(300, 31, 0.60, "历史帖子三百第一条")
                ));

        RagRetrievalService service = new RagRetrievalService(properties, embeddingClient, vectorStoreClient);
        RagRetrievalService.RetrievalResult result = service.retrieve("查询", 999);

        assertFalse(result.relatedPosts().stream().anyMatch(post -> post.getPostId() == 999));
        assertEquals(3, result.relatedPosts().size());
        assertEquals(100, result.relatedPosts().get(0).getPostId());
        assertEquals(3, result.relatedPosts().get(0).getEvidenceCount());
        assertTrue(result.relatedReplies().stream().noneMatch(reply -> reply.getPostId() == 999));
        assertTrue(result.relatedReplies().stream()
                .filter(reply -> reply.getPostId() == 100)
                .count() <= 2);
    }

    @Test
    void fusesOriginalAndRewrittenPathsByRrf() {
        AgentProperties properties = properties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);

        when(embeddingClient.embed("原始查询")).thenReturn(new float[1024]);
        when(embeddingClient.embed("改写查询")).thenReturn(new float[1024]);
        when(vectorStoreClient.search(any(float[].class), anyInt(), eq(0.55), eq(999)))
                .thenReturn(
                        List.of(
                                hit(100, 11, 0.60, "原文命中"),
                                hit(100, 12, 0.59, "原文命中二")
                        ),
                        List.of(
                                hit(200, 21, 0.70, "改写命中"),
                                hit(100, 11, 0.61, "改写也命中")
                        )
                );

        RagRetrievalService service = new RagRetrievalService(properties, embeddingClient, vectorStoreClient);
        RagRetrievalService.RetrievalResult result = service.retrieve("原始查询", "改写查询", 999);

        // 同一片段被两路召回，RRF 融合后应排第一并合并来源
        VectorSearchResult top = result.hits().get(0);
        assertEquals(100, top.getPostId());
        assertEquals(11, top.getCommentId());
        assertEquals("HYBRID", top.getRetrievalSource());
        assertEquals(0.61, top.getScore(), 1e-9);
        assertEquals(100, result.relatedPosts().get(0).getPostId());
        assertEquals("改写查询", result.rewrittenQuery());
        assertFalse(result.weak());
    }

    @Test
    void fallsBackToRelaxedThresholdAndMarksWeakEvidence() {
        AgentProperties properties = properties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);

        when(embeddingClient.embed(any())).thenReturn(new float[1024]);
        when(vectorStoreClient.search(any(float[].class), anyInt(), eq(0.55), eq(999)))
                .thenReturn(List.of());
        when(vectorStoreClient.search(any(float[].class), anyInt(), eq(0.45), eq(999)))
                .thenReturn(List.of(hit(400, 41, 0.50, "弱相关历史")));

        RagRetrievalService service = new RagRetrievalService(properties, embeddingClient, vectorStoreClient);
        RagRetrievalService.RetrievalResult result = service.retrieve("查询", 999);

        assertTrue(result.weak());
        assertEquals(1, result.relatedPosts().size());
        assertEquals(400, result.relatedPosts().get(0).getPostId());
    }

    @Test
    void keepsOriginalPathWhenRewriteSearchFails() {
        AgentProperties properties = properties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);

        when(embeddingClient.embed("原始查询")).thenReturn(new float[1024]);
        when(embeddingClient.embed("改写查询")).thenThrow(new RagUnavailableException("Ollama 不可用"));
        when(vectorStoreClient.search(any(float[].class), anyInt(), eq(0.55), eq(999)))
                .thenReturn(List.of(hit(100, 11, 0.90, "原文命中")));

        RagRetrievalService service = new RagRetrievalService(properties, embeddingClient, vectorStoreClient);
        RagRetrievalService.RetrievalResult result = service.retrieve("原始查询", "改写查询", 999);

        assertFalse(result.weak());
        assertEquals(1, result.relatedPosts().size());
        assertEquals(100, result.relatedPosts().get(0).getPostId());
    }

    private AgentProperties properties() {
        AgentProperties properties = new AgentProperties();
        properties.getRag().setTopK(8);
        properties.getRag().setScoreThreshold(0.55);
        properties.getRag().setMaxRelatedPosts(5);
        properties.getRag().setMaxRelatedReplies(8);
        properties.getRag().setMaxRepliesPerPost(2);
        return properties;
    }

    private VectorSearchResult hit(long postId, long commentId, double score, String content) {
        VectorSearchResult hit = new VectorSearchResult();
        hit.setId("post:" + postId + ":comment:" + commentId + ":v1");
        hit.setPostId(postId);
        hit.setCommentId(commentId);
        hit.setScore(score);
        hit.setTitle("历史帖子 " + postId);
        hit.setCategory("打听求助");
        hit.setSourceType("POST_COMMENT");
        hit.setContent("【帖子标题】历史帖子 " + postId + "\n【主评论】" + content);
        hit.setCreatedAt(System.currentTimeMillis());
        hit.setStatus("ACTIVE");
        hit.setDocVersion(1);
        return hit;
    }
}
