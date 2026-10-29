package software_market_agent.rag;

import org.junit.jupiter.api.Test;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RrfFuserTest {

    @Test
    void mergesDuplicateIdsAndRanksMultiPathHitsHigher() {
        VectorSearchResult b = hit("b", 0.80);
        VectorSearchResult a = hit("a", 0.90);
        VectorSearchResult c = hit("c", 0.70);
        VectorSearchResult aAgain = hit("a", 0.60);

        List<VectorSearchResult> fused = RrfFuser.fuse(List.of(
                new RrfFuser.RetrievalPath(RrfFuser.originalSource(), List.of(b, a)),
                new RrfFuser.RetrievalPath(RrfFuser.rewrittenSource(), List.of(c, aAgain))
        ));

        assertEquals(3, fused.size());
        VectorSearchResult top = fused.get(0);
        assertEquals("a", top.getId());
        assertEquals("HYBRID", top.getRetrievalSource());
        // 原始分保留两路余弦最大值；a 在两路中都排第 2
        assertEquals(0.90, top.getScore(), 1e-9);
        assertEquals(2.0 / 62, top.getRrfScore(), 1e-9);

        // b 与 c 同分，按原始余弦分降序
        assertEquals("b", fused.get(1).getId());
        assertEquals("ORIGINAL", fused.get(1).getRetrievalSource());
        assertEquals("c", fused.get(2).getId());
        assertEquals("REWRITTEN", fused.get(2).getRetrievalSource());
    }

    @Test
    void singlePathPreservesRankOrder() {
        VectorSearchResult first = hit("first", 0.9);
        VectorSearchResult second = hit("second", 0.8);

        List<VectorSearchResult> fused = RrfFuser.fuse(List.of(
                new RrfFuser.RetrievalPath(RrfFuser.originalSource(), List.of(first, second))
        ));

        assertEquals("first", fused.get(0).getId());
        assertEquals("second", fused.get(1).getId());
        assertTrue(fused.get(0).getRrfScore() > fused.get(1).getRrfScore());
    }

    @Test
    void emptyInputReturnsEmptyList() {
        assertTrue(RrfFuser.fuse(List.of()).isEmpty());
    }

    private VectorSearchResult hit(String id, double score) {
        VectorSearchResult hit = new VectorSearchResult();
        hit.setId(id);
        hit.setScore(score);
        return hit;
    }
}
