package software_market_agent.rag;

import org.springframework.util.StringUtils;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reciprocal Rank Fusion：把多路有序召回列表合成一份候选集。
 * 不同查询路径的余弦分不可直接相加，RRF 只按排名累加 1/(k+rank)，
 * 同一片段在多路都靠前时融合分会更高。片段按 Milvus 主键去重，
 * 原始 score 保留各路余弦最大值供展示使用。
 */
public final class RrfFuser {

    public static final int DEFAULT_K = 60;

    private static final String SOURCE_ORIGINAL = "ORIGINAL";
    private static final String SOURCE_REWRITTEN = "REWRITTEN";
    private static final String SOURCE_HYBRID = "HYBRID";

    private RrfFuser() {
    }

    public record RetrievalPath(String source, List<VectorSearchResult> hits) {
    }

    public static String originalSource() {
        return SOURCE_ORIGINAL;
    }

    public static String rewrittenSource() {
        return SOURCE_REWRITTEN;
    }

    public static List<VectorSearchResult> fuse(List<RetrievalPath> paths) {
        return fuse(paths, DEFAULT_K);
    }

    public static List<VectorSearchResult> fuse(List<RetrievalPath> paths, int k) {
        if (paths == null || paths.isEmpty()) {
            return List.of();
        }
        int safeK = Math.max(1, k);
        Map<String, VectorSearchResult> byId = new LinkedHashMap<>();
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, String> sources = new HashMap<>();

        for (RetrievalPath path : paths) {
            if (path == null || path.hits() == null) {
                continue;
            }
            List<VectorSearchResult> hits = path.hits();
            for (int i = 0; i < hits.size(); i++) {
                VectorSearchResult hit = hits.get(i);
                if (hit == null || !StringUtils.hasText(hit.getId())) {
                    continue;
                }
                String id = hit.getId();
                VectorSearchResult existing = byId.get(id);
                if (existing == null) {
                    byId.put(id, hit);
                } else {
                    existing.setScore(Math.max(existing.getScore(), hit.getScore()));
                }
                rrfScores.merge(id, 1.0 / (safeK + i + 1), Double::sum);
                sources.merge(id, path.source(), RrfFuser::mergeSource);
            }
        }

        List<VectorSearchResult> fused = new ArrayList<>(byId.values());
        for (VectorSearchResult hit : fused) {
            hit.setRrfScore(rrfScores.getOrDefault(hit.getId(), 0.0));
            hit.setRetrievalSource(sources.get(hit.getId()));
        }
        fused.sort(Comparator
                .comparingDouble(VectorSearchResult::getRrfScore).reversed()
                .thenComparing(Comparator.comparingDouble(VectorSearchResult::getScore).reversed()));
        return fused;
    }

    private static String mergeSource(String left, String right) {
        if (left == null) {
            return right;
        }
        return left.equals(right) ? left : SOURCE_HYBRID;
    }
}
