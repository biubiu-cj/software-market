package software_market_agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software_market_agent.config.AgentProperties;
import software_market_agent.embedding.EmbeddingClient;
import software_market_agent.exception.RagUnavailableException;
import software_market_agent.milvus.VectorStoreClient;
import software_market_agent.model.internal.AiRelatedPost;
import software_market_agent.model.internal.AiRelatedReply;
import software_market_agent.model.milvus.VectorSearchResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final int MAX_QUERY_LENGTH = 2000;

    private final AgentProperties properties;
    private final EmbeddingClient embeddingClient;
    private final VectorStoreClient vectorStoreClient;

    public RetrievalResult retrieve(String query, Integer excludePostId) {
        return retrieve(query, null, excludePostId);
    }

    /**
     * 原文与改写查询各召回一路，RRF 融合后按名次排序。
     * 改写路径失败时只保留原文路径；严格阈值全部落空时用回退阈值再检索一次，结果标记为弱证据。
     */
    public RetrievalResult retrieve(String originalQuery, String rewrittenQuery, Integer excludePostId) {
        String normalizedOriginal = truncate(originalQuery, MAX_QUERY_LENGTH);
        String normalizedRewritten = normalizeQuery(rewrittenQuery);
        double threshold = properties.getRag().getScoreThreshold();

        List<VectorSearchResult> hits = fuseSearch(normalizedOriginal, normalizedRewritten, excludePostId, threshold);
        boolean weak = false;
        double fallbackThreshold = properties.getRag().getFallbackScoreThreshold();
        if (hits.isEmpty() && fallbackThreshold > 0 && fallbackThreshold < threshold) {
            hits = fuseSearch(normalizedOriginal, normalizedRewritten, excludePostId, fallbackThreshold);
            weak = !hits.isEmpty();
            if (weak) {
                log.info("严格阈值无命中，回退到 {} 后命中 {} 条弱证据", fallbackThreshold, hits.size());
            }
        }

        List<VectorSearchResult> filtered = hits.stream()
                .filter(hit -> excludePostId == null || hit.getPostId() != excludePostId)
                .toList();
        if (filtered.isEmpty()) {
            return new RetrievalResult(List.of(), List.of(), List.of(), weak, normalizedRewritten);
        }

        List<AiRelatedPost> relatedPosts = aggregatePosts(filtered);
        List<AiRelatedReply> relatedReplies = aggregateReplies(filtered);
        log.info("检索完成: originalQuery={}, rewrittenQuery={}, threshold={}, hits={}, weak={}",
                abbreviate(normalizedOriginal), normalizedRewritten, threshold, filtered.size(), weak);
        return new RetrievalResult(filtered, relatedPosts, relatedReplies, weak, normalizedRewritten);
    }

    private List<VectorSearchResult> fuseSearch(String originalQuery,
                                                String rewrittenQuery,
                                                Integer excludePostId,
                                                double threshold) {
        List<RrfFuser.RetrievalPath> paths = new ArrayList<>();
        paths.add(new RrfFuser.RetrievalPath(
                RrfFuser.originalSource(),
                search(originalQuery, threshold, excludePostId)
        ));
        if (StringUtils.hasText(rewrittenQuery) && !rewrittenQuery.equals(originalQuery)) {
            try {
                paths.add(new RrfFuser.RetrievalPath(
                        RrfFuser.rewrittenSource(),
                        search(rewrittenQuery, threshold, excludePostId)
                ));
            } catch (RagUnavailableException e) {
                // 改写路径失败不能拖垮整次分析，退回单路召回
                log.warn("改写查询检索失败，仅使用原文召回: {}", e.getMessage());
            }
        }
        return RrfFuser.fuse(paths, properties.getRag().getRrfK());
    }

    private List<VectorSearchResult> search(String query, double threshold, Integer excludePostId) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        float[] queryVector = embeddingClient.embed(query);
        List<VectorSearchResult> hits = vectorStoreClient.search(
                queryVector,
                properties.getRag().getTopK(),
                threshold,
                excludePostId
        );
        return hits == null ? List.of() : hits;
    }

    private List<AiRelatedPost> aggregatePosts(List<VectorSearchResult> hits) {
        Map<Long, List<VectorSearchResult>> grouped = new LinkedHashMap<>();
        for (VectorSearchResult hit : hits) {
            grouped.computeIfAbsent(hit.getPostId(), ignored -> new ArrayList<>()).add(hit);
        }
        List<Map.Entry<Long, List<VectorSearchResult>>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort((a, b) -> Double.compare(bestRrfScore(b.getValue()), bestRrfScore(a.getValue())));

        List<AiRelatedPost> result = new ArrayList<>();
        for (Map.Entry<Long, List<VectorSearchResult>> entry : entries) {
            if (result.size() >= properties.getRag().getMaxRelatedPosts()) {
                break;
            }
            List<VectorSearchResult> postHits = entry.getValue();
            VectorSearchResult best = postHits.get(0);
            AiRelatedPost post = new AiRelatedPost();
            post.setPostId(Math.toIntExact(entry.getKey()));
            post.setTitle(best.getTitle());
            post.setCategory(best.getCategory());
            post.setSummary(snippet(best.getContent(), 120));
            post.setCreatedAt(formatTime(best.getCreatedAt()));
            post.setMatchScore(bestScore(postHits));
            post.setEvidenceCount(postHits.size());
            post.setUrl("/postdetail/" + entry.getKey());
            result.add(post);
        }
        return result;
    }

    private List<AiRelatedReply> aggregateReplies(List<VectorSearchResult> hits) {
        List<AiRelatedReply> result = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        Map<Long, Integer> perPost = new LinkedHashMap<>();
        for (VectorSearchResult hit : hits) {
            if (!"POST_COMMENT".equals(hit.getSourceType()) || hit.getCommentId() <= 0) {
                continue;
            }
            String key = hit.getPostId() + ":" + hit.getCommentId();
            if (!dedup.add(key)) {
                continue;
            }
            int count = perPost.getOrDefault(hit.getPostId(), 0);
            if (count >= properties.getRag().getMaxRepliesPerPost()) {
                continue;
            }
            perPost.put(hit.getPostId(), count + 1);

            AiRelatedReply reply = new AiRelatedReply();
            reply.setPostId(Math.toIntExact(hit.getPostId()));
            reply.setCommentId(Math.toIntExact(hit.getCommentId()));
            reply.setTitle(hit.getTitle());
            reply.setSourceType(hit.getSourceType());
            reply.setContent(snippet(hit.getContent(), 220));
            reply.setCreatedAt(formatTime(hit.getCreatedAt()));
            reply.setScore(hit.getScore());
            reply.setUrl("/postdetail/" + hit.getPostId() + "?commentId=" + hit.getCommentId());
            result.add(reply);
            if (result.size() >= properties.getRag().getMaxRelatedReplies()) {
                break;
            }
        }
        return result;
    }

    private double bestScore(List<VectorSearchResult> hits) {
        return hits.stream().mapToDouble(VectorSearchResult::getScore).max().orElse(0);
    }

    private double bestRrfScore(List<VectorSearchResult> hits) {
        return hits.stream().mapToDouble(VectorSearchResult::getRrfScore).max().orElse(0);
    }

    /**
     * 去掉文档头部的标题/分区信息，只展示用户可见的正文片段。
     */
    private String snippet(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        String text = content;
        int bodyIndex = text.indexOf("【正文】");
        int commentIndex = text.indexOf("【主评论】");
        int start = bodyIndex >= 0 ? bodyIndex : commentIndex;
        if (start >= 0) {
            text = text.substring(start);
        }
        text = text.replace('\n', ' ').strip();
        return truncate(text, maxLength);
    }

    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) {
            return "";
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return truncate(query.strip(), MAX_QUERY_LENGTH);
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        String singleLine = text.replace('\n', ' ').strip();
        return singleLine.length() <= 80 ? singleLine : singleLine.substring(0, 80) + "...";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    public record RetrievalResult(List<VectorSearchResult> hits,
                                  List<AiRelatedPost> relatedPosts,
                                  List<AiRelatedReply> relatedReplies,
                                  boolean weak,
                                  String rewrittenQuery) {
    }
}
