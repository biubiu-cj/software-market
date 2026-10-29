package software_market_agent.eval.metrics;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.RetrievalScores;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检索层指标：Hit@K、MRR、Recall@K。
 * 回复键（postId:commentId）优先；否则按帖子 ID 或标题判断相关性。
 */
@Component
public class RetrievalMetricCalculator {

    public RetrievalScores calculate(List<VectorSearchResult> hits, EvalCase evalCase, int topK) {
        if (hits == null || hits.isEmpty() || evalCase == null || !evalCase.hasRetrievalGroundTruth()) {
            return null;
        }
        int limit = topK > 0 ? Math.min(topK, hits.size()) : hits.size();
        List<VectorSearchResult> top = hits.subList(0, limit);

        Set<String> gtReplyKeys = toSet(evalCase.getGroundTruthReplyKeys());
        if (!gtReplyKeys.isEmpty()) {
            return calculateReplyLevel(top, gtReplyKeys);
        }
        Set<Long> gtPostIds = toSet(evalCase.getGroundTruthPostIds());
        if (!gtPostIds.isEmpty()) {
            return calculatePostLevel(top, gtPostIds);
        }
        Set<String> gtTitles = toSet(evalCase.getGroundTruthPostTitles());
        if (!gtTitles.isEmpty()) {
            return calculateTitleLevel(top, gtTitles);
        }
        return null;
    }

    private RetrievalScores calculateReplyLevel(List<VectorSearchResult> top, Set<String> gtKeys) {
        boolean hit = false;
        double mrr = 0;
        Set<String> matched = new HashSet<>();
        for (int i = 0; i < top.size(); i++) {
            VectorSearchResult hitItem = top.get(i);
            if (hitItem.getCommentId() <= 0) {
                continue;
            }
            String key = hitItem.getPostId() + ":" + hitItem.getCommentId();
            if (!gtKeys.contains(key)) {
                continue;
            }
            if (mrr == 0) {
                mrr = 1.0 / (i + 1);
            }
            hit = true;
            matched.add(key);
        }
        return new RetrievalScores(hit, mrr, (double) matched.size() / gtKeys.size());
    }

    private RetrievalScores calculatePostLevel(List<VectorSearchResult> top, Set<Long> gtPostIds) {
        boolean hit = false;
        double mrr = 0;
        Set<Long> matched = new HashSet<>();
        for (int i = 0; i < top.size(); i++) {
            VectorSearchResult hitItem = top.get(i);
            if (!gtPostIds.contains(hitItem.getPostId())) {
                continue;
            }
            if (mrr == 0) {
                mrr = 1.0 / (i + 1);
            }
            hit = true;
            matched.add(hitItem.getPostId());
        }
        return new RetrievalScores(hit, mrr, (double) matched.size() / gtPostIds.size());
    }

    private RetrievalScores calculateTitleLevel(List<VectorSearchResult> top, Set<String> gtTitles) {
        boolean hit = false;
        double mrr = 0;
        Set<String> matched = new HashSet<>();
        for (int i = 0; i < top.size(); i++) {
            VectorSearchResult hitItem = top.get(i);
            String title = hitItem.getTitle() == null ? "" : hitItem.getTitle().strip();
            if (!gtTitles.contains(title)) {
                continue;
            }
            if (mrr == 0) {
                mrr = 1.0 / (i + 1);
            }
            hit = true;
            matched.add(title);
        }
        return new RetrievalScores(hit, mrr, (double) matched.size() / gtTitles.size());
    }

    private <T> Set<T> toSet(List<T> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<T> result = new HashSet<>();
        for (T value : values) {
            if (value instanceof String text) {
                if (StringUtils.hasText(text)) {
                    result.add(value);
                }
            } else if (value != null) {
                result.add(value);
            }
        }
        return result;
    }
}
