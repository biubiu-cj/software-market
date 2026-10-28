package software_market_agent.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software_market_agent.config.AgentProperties;
import software_market_agent.embedding.EmbeddingClient;
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
        String normalizedQuery = truncate(query, MAX_QUERY_LENGTH);
        float[] queryVector = embeddingClient.embed(normalizedQuery);
        List<VectorSearchResult> hits = vectorStoreClient.search(
                queryVector,
                properties.getRag().getTopK(),
                properties.getRag().getScoreThreshold(),
                excludePostId
        );
        if (hits == null || hits.isEmpty()) {
            return new RetrievalResult(List.of(), List.of(), List.of());
        }

        List<VectorSearchResult> filtered = hits.stream()
                .filter(hit -> excludePostId == null || hit.getPostId() != excludePostId)
                .sorted(Comparator.comparingDouble(VectorSearchResult::getScore).reversed())
                .toList();

        List<AiRelatedPost> relatedPosts = aggregatePosts(filtered);
        List<AiRelatedReply> relatedReplies = aggregateReplies(filtered);
        return new RetrievalResult(filtered, relatedPosts, relatedReplies);
    }

    private List<AiRelatedPost> aggregatePosts(List<VectorSearchResult> hits) {
        Map<Long, List<VectorSearchResult>> grouped = new LinkedHashMap<>();
        for (VectorSearchResult hit : hits) {
            grouped.computeIfAbsent(hit.getPostId(), ignored -> new ArrayList<>()).add(hit);
        }
        List<Map.Entry<Long, List<VectorSearchResult>>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort((a, b) -> Double.compare(bestScore(b.getValue()), bestScore(a.getValue())));

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

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    public record RetrievalResult(List<VectorSearchResult> hits,
                                  List<AiRelatedPost> relatedPosts,
                                  List<AiRelatedReply> relatedReplies) {
    }
}
