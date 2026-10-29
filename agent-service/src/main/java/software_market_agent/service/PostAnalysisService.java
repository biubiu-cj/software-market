package software_market_agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software_market_agent.agentscope.ChatModelProvider;
import software_market_agent.agentscope.PostAnalysisAgentBuilder;
import software_market_agent.config.AgentProperties;
import software_market_agent.exception.ModelCallException;
import software_market_agent.exception.RagUnavailableException;
import software_market_agent.model.internal.AiCitation;
import software_market_agent.model.internal.AiRelatedReply;
import software_market_agent.model.internal.PostAnalysisRequest;
import software_market_agent.model.internal.PostAnalysisResponse;
import software_market_agent.model.milvus.VectorSearchResult;
import software_market_agent.rag.QueryRewriteService;
import software_market_agent.rag.RagRetrievalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAnalysisService {

    public static final String READY = "READY";
    public static final String NO_HISTORY = "NO_HISTORY";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String MODEL_ERROR = "MODEL_ERROR";

    private static final String NOTICE = "以上内容来自站内历史讨论，仅供参考。";
    private static final int MAX_CONCLUSION_LENGTH = 2000;
    private static final int MAX_PROMPT_CONTENT = 2000;
    private static final int MAX_DOC_CONTENT = 1200;
    private static final int MAX_CITATIONS = 5;
    private static final Pattern CITATION_PATTERN = Pattern.compile("\\[(\\d+)]");

    private final AgentProperties properties;
    private final RagRetrievalService retrievalService;
    private final QueryRewriteService queryRewriteService;
    private final ChatModelProvider chatModelProvider;
    private final PostAnalysisAgentBuilder agentBuilder;
    private final ObjectMapper objectMapper;

    public PostAnalysisResponse analyze(PostAnalysisRequest request) {
        long startedAt = System.nanoTime();
        RagRetrievalService.RetrievalResult retrieval;
        try {
            retrieval = prepare(request);
        } catch (RagUnavailableException e) {
            log.warn("RAG 检索不可用: postId={}, error={}", request.getPostId(), e.getMessage());
            PostAnalysisResponse response = baseResponse();
            response.setStatus(SERVICE_UNAVAILABLE);
            response.setConclusion("AI 检索服务暂时不可用，不影响帖子浏览、评论和点赞。");
            return finish(response, startedAt);
        }
        return analyzeWithRetrieval(request, retrieval, baseResponse(), startedAt).response();
    }

    /**
     * 检索阶段：LLM 改写 + 双路召回 + RRF 融合。评测也复用这一步，保证评测与线上链路一致。
     */
    public RagRetrievalService.RetrievalResult prepare(PostAnalysisRequest request) {
        String query = safe(request.getTitle()) + "\n" + safe(request.getContent());
        String rewritten = queryRewriteService.rewrite(request.getTitle(), request.getContent());
        return retrievalService.retrieve(query, rewritten, request.getPostId());
    }

    public AnalysisOutcome analyzeWithRetrieval(PostAnalysisRequest request,
                                                RagRetrievalService.RetrievalResult retrieval) {
        return analyzeWithRetrieval(request, retrieval, baseResponse(), System.nanoTime());
    }

    private AnalysisOutcome analyzeWithRetrieval(PostAnalysisRequest request,
                                                 RagRetrievalService.RetrievalResult retrieval,
                                                 PostAnalysisResponse response,
                                                 long startedAt) {
        List<VectorSearchResult> contextHits = dedupeHits(retrieval.hits());
        int maxContextDocs = Math.max(1, properties.getRag().getMaxContextDocs());
        if (contextHits.size() > maxContextDocs) {
            contextHits = contextHits.subList(0, maxContextDocs);
        }
        Map<String, Integer> citationIndexByKey = buildCitationIndex(contextHits);

        response.setRelatedPosts(retrieval.relatedPosts());
        response.setRelatedReplies(retrieval.relatedReplies());
        if (retrieval.weak()) {
            // 弱证据只展示相似度较低的相关帖子，不生成结论
            response.setStatus(NO_HISTORY);
            response.setConclusion("没有找到足够相关的历史讨论，下面列出的是相似度较低的历史帖子，仅供参考。");
            return new AnalysisOutcome(finish(response, startedAt), contextHits);
        }
        if (contextHits.isEmpty()) {
            response.setStatus(NO_HISTORY);
            response.setConclusion("站内暂无足够相关的历史讨论，AI 不会凭空回答。");
            return new AnalysisOutcome(finish(response, startedAt), contextHits);
        }

        if (!chatModelProvider.isAvailable()) {
            return new AnalysisOutcome(extractiveFallback(response, contextHits, startedAt), contextHits);
        }

        try {
            String modelOutput = callModel(request, contextHits);
            ParsedOutput parsed = parseOutput(modelOutput);
            if (!parsed.answerable()) {
                response.setStatus(NO_HISTORY);
                response.setConclusion(parsed.conclusion().isBlank()
                        ? "站内历史讨论中没有足够依据回答当前问题。"
                        : parsed.conclusion());
                return new AnalysisOutcome(finish(response, startedAt), contextHits);
            }

            ConclusionWithCitations normalized = normalizeCitations(parsed, contextHits);
            if (normalized.citations().isEmpty()) {
                response.setStatus(NO_HISTORY);
                response.setConclusion("模型没有给出可核实的历史引用，暂不展示未经核实的结论。");
                return new AnalysisOutcome(finish(response, startedAt), contextHits);
            }
            response.setStatus(READY);
            response.setConclusion(normalized.conclusion());
            response.setCitations(normalized.citations());
            applyCitedIndexes(response, normalized.citations(), contextHits, citationIndexByKey);
            response.setModel(properties.getChat().getModel());
            return new AnalysisOutcome(finish(response, startedAt), contextHits);
        } catch (ModelCallException e) {
            log.warn("大模型调用失败: postId={}, error={}", request.getPostId(), e.getMessage());
            response.setStatus(MODEL_ERROR);
            response.setConclusion("大模型调用失败，请稍后重试。帖子内容和评论不受影响。");
            return new AnalysisOutcome(finish(response, startedAt), contextHits);
        } catch (Exception e) {
            log.error("AI 分析异常: postId={}", request.getPostId(), e);
            response.setStatus(MODEL_ERROR);
            response.setConclusion("AI 分析失败，请稍后重试。");
            return new AnalysisOutcome(finish(response, startedAt), contextHits);
        }
    }

    /**
     * 同一评论可能被切成多个 chunk；用于引用时只保留分数最高的一条，
     * 保证 conclusion 中的 [n] 与相关历史回复一一对应。
     */
    private List<VectorSearchResult> dedupeHits(List<VectorSearchResult> hits) {
        Map<String, VectorSearchResult> unique = new LinkedHashMap<>();
        for (VectorSearchResult hit : hits) {
            unique.putIfAbsent(key(hit.getPostId(), hit.getCommentId()), hit);
        }
        return new ArrayList<>(unique.values());
    }

    private Map<String, Integer> buildCitationIndex(List<VectorSearchResult> hits) {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        for (int i = 0; i < hits.size(); i++) {
            VectorSearchResult hit = hits.get(i);
            indexes.putIfAbsent(key(hit.getPostId(), hit.getCommentId()), i + 1);
        }
        return indexes;
    }

    /**
     * 只给“结论真正引用”的回复打 [n]，并把因每帖限流而未展示的引用回复补进来。
     */
    private void applyCitedIndexes(PostAnalysisResponse response,
                                   List<AiCitation> citations,
                                   List<VectorSearchResult> contextHits,
                                   Map<String, Integer> citationIndexByKey) {
        List<AiRelatedReply> replies = response.getRelatedReplies();
        if (replies == null) {
            replies = new ArrayList<>();
            response.setRelatedReplies(replies);
        }
        Map<String, Integer> citedIndexByKey = new LinkedHashMap<>();
        for (AiCitation citation : citations) {
            if (citation.getPostId() != null && citation.getCommentId() != null) {
                citedIndexByKey.put(key(citation.getPostId(), citation.getCommentId()), citation.getIndex());
            }
        }
        for (AiRelatedReply reply : replies) {
            reply.setCitationIndex(citedIndexByKey.getOrDefault(
                    key(reply.getPostId(), reply.getCommentId()), 0));
        }

        Set<String> existingKeys = new LinkedHashSet<>();
        for (AiRelatedReply reply : replies) {
            existingKeys.add(key(reply.getPostId(), reply.getCommentId()));
        }
        Map<String, VectorSearchResult> hitsByKey = new LinkedHashMap<>();
        for (VectorSearchResult hit : contextHits) {
            hitsByKey.putIfAbsent(key(hit.getPostId(), hit.getCommentId()), hit);
        }
        for (AiCitation citation : citations) {
            if (citation.getCommentId() == null || citation.getCommentId() <= 0) {
                continue;
            }
            String citationKey = key(citation.getPostId(), citation.getCommentId());
            if (!existingKeys.add(citationKey)) {
                continue;
            }
            VectorSearchResult hit = hitsByKey.get(citationKey);
            if (hit != null) {
                replies.add(toRelatedReply(hit, citation.getIndex()));
            }
        }

        // 被引用的回复排在前面，按 [n] 排序；其余相关回复排在后面
        replies.sort(java.util.Comparator.comparingInt(reply ->
                reply.getCitationIndex() == null || reply.getCitationIndex() <= 0
                        ? Integer.MAX_VALUE
                        : reply.getCitationIndex()));
        int maxReplies = Math.max(1, properties.getRag().getMaxRelatedReplies());
        if (replies.size() > maxReplies) {
            response.setRelatedReplies(new ArrayList<>(replies.subList(0, maxReplies)));
        }
    }

    private AiRelatedReply toRelatedReply(VectorSearchResult hit, Integer citationIndex) {
        AiRelatedReply reply = new AiRelatedReply();
        reply.setPostId(Math.toIntExact(hit.getPostId()));
        reply.setCommentId(Math.toIntExact(hit.getCommentId()));
        reply.setCitationIndex(citationIndex);
        reply.setTitle(hit.getTitle());
        reply.setSourceType(hit.getSourceType());
        reply.setContent(snippet(hit.getContent(), 220));
        reply.setCreatedAt(formatTime(hit.getCreatedAt()));
        reply.setScore(hit.getScore());
        reply.setUrl("/postdetail/" + hit.getPostId() + "?commentId=" + hit.getCommentId());
        return reply;
    }

    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) {
            return "";
        }
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(epochMillis));
    }

    private String callModel(PostAnalysisRequest request, List<VectorSearchResult> hits) {
        try (ReActAgent agent = agentBuilder.build()) {
            RuntimeContext context = RuntimeContext.builder()
                    .sessionId("post-analysis-" + request.getPostId())
                    .userId("system")
                    .build();
            Msg message = agent.call(buildUserPrompt(request, hits), context)
                    .block(properties.getChat().getTimeout());
            if (message == null || !org.springframework.util.StringUtils.hasText(message.getTextContent())) {
                throw new ModelCallException("模型返回为空");
            }
            return message.getTextContent();
        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCallException("模型调用失败", e);
        }
    }

    private String buildUserPrompt(PostAnalysisRequest request, List<VectorSearchResult> hits) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("【当前帖子】\n");
        prompt.append("标题：").append(safe(request.getTitle())).append("\n");
        prompt.append("分区：").append(safe(request.getCategory())).append("\n");
        prompt.append("标签：").append(safe(request.getTag())).append("\n");
        prompt.append("正文：").append(truncate(safe(request.getContent()), MAX_PROMPT_CONTENT)).append("\n\n");
        prompt.append("【历史资料】\n<history>\n");
        for (int i = 0; i < hits.size(); i++) {
            VectorSearchResult hit = hits.get(i);
            prompt.append("<doc index=\"").append(i + 1)
                    .append("\" postId=\"").append(hit.getPostId())
                    .append("\" commentId=\"").append(hit.getCommentId())
                    .append("\" sourceType=\"").append(hit.getSourceType())
                    .append("\">\n");
            prompt.append(truncate(hit.getContent(), MAX_DOC_CONTENT)).append("\n</doc>\n");
        }
        prompt.append("</history>\n\n");
        prompt.append("""
                请输出 JSON：
                {
                  "answerable": true,
                  "conclusion": "你自己的中文总结，在关键结论后用 [1]、[2] 标注依据",
                  "cited": [1, 2]
                }
                规则：
                1. conclusion 必须是自己的总结，不要整段复制历史原文。
                2. 每个关键结论后必须用 [n] 标注依据，n 只能来自上面 <doc index="n"> 的编号。
                3. 不要引用没有出现的编号；没有足够依据时 answerable=false。
                4. cited 填写实际引用的编号数组，与 conclusion 中的 [n] 保持一致。
                """);
        return prompt.toString();
    }

    private ParsedOutput parseOutput(String content) {
        String cleaned = content == null ? "" : content
                .replace("```json", "")
                .replace("```", "")
                .strip();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new ModelCallException("模型未返回合法 JSON");
        }
        try {
            JsonNode root = objectMapper.readTree(cleaned.substring(start, end + 1));
            boolean answerable = root.path("answerable").asBoolean(false);
            String conclusion = root.path("conclusion").asText("");
            List<Integer> cited = new ArrayList<>();
            JsonNode citedNode = root.path("cited");
            if (citedNode.isArray()) {
                for (JsonNode item : citedNode) {
                    if (item.isNumber()) {
                        cited.add(item.asInt());
                    } else if (item.has("index")) {
                        cited.add(item.path("index").asInt());
                    }
                }
            }
            return new ParsedOutput(answerable, conclusion, cited);
        } catch (Exception e) {
            throw new ModelCallException("解析模型 JSON 失败", e);
        }
    }

    /**
     * 校验 conclusion 中的 [n]，删除不存在的编号；没有标记时用 cited 补到末尾。
     */
    private ConclusionWithCitations normalizeCitations(ParsedOutput parsed,
                                                       List<VectorSearchResult> hits) {
        int maxIndex = hits.size();
        Set<Integer> validIndexes = new LinkedHashSet<>();
        Matcher matcher = CITATION_PATTERN.matcher(parsed.conclusion());
        StringBuffer cleaned = new StringBuffer();
        List<Integer> markers = new ArrayList<>();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index >= 1 && index <= maxIndex) {
                markers.add(index);
                matcher.appendReplacement(cleaned, matcher.group());
            } else {
                matcher.appendReplacement(cleaned, "");
            }
        }
        matcher.appendTail(cleaned);

        String conclusion = cleaned.toString().strip();
        if (markers.isEmpty()) {
            for (Integer index : parsed.cited()) {
                if (index != null && index >= 1 && index <= maxIndex) {
                    validIndexes.add(index);
                }
            }
            if (!validIndexes.isEmpty()) {
                StringBuilder suffix = new StringBuilder();
                for (Integer index : validIndexes) {
                    suffix.append(" [").append(index).append(']');
                }
                conclusion = conclusion + suffix;
            }
        } else {
            validIndexes.addAll(markers);
        }

        if (conclusion.isBlank() && !validIndexes.isEmpty()) {
            conclusion = "根据站内历史讨论，相关问题可参考以下历史回复。";
        }

        List<Integer> ordered = new ArrayList<>(validIndexes);
        if (ordered.size() > MAX_CITATIONS) {
            ordered = ordered.subList(0, MAX_CITATIONS);
        }
        List<AiCitation> citations = buildCitations(ordered, hits);
        return new ConclusionWithCitations(
                truncate(conclusion, MAX_CONCLUSION_LENGTH),
                citations
        );
    }

    private List<AiCitation> buildCitations(List<Integer> indexes, List<VectorSearchResult> hits) {
        List<AiCitation> citations = new ArrayList<>();
        for (Integer index : indexes) {
            if (index == null || index < 1 || index > hits.size()) {
                continue;
            }
            VectorSearchResult hit = hits.get(index - 1);
            AiCitation citation = new AiCitation();
            citation.setIndex(index);
            citation.setPostId(Math.toIntExact(hit.getPostId()));
            citation.setCommentId(Math.toIntExact(hit.getCommentId()));
            citation.setTitle(hit.getTitle());
            citation.setUrl("/postdetail/" + hit.getPostId()
                    + (hit.getCommentId() > 0 ? "?commentId=" + hit.getCommentId() : ""));
            citations.add(citation);
        }
        return citations;
    }

    /**
     * 没有配置大模型时，用检索结果生成带引用的摘要，不编造内容。
     */
    private PostAnalysisResponse extractiveFallback(PostAnalysisResponse response,
                                                    List<VectorSearchResult> hits,
                                                    long startedAt) {
        List<VectorSearchResult> top = hits.stream().limit(3).toList();
        StringBuilder conclusion = new StringBuilder("根据站内历史讨论，");
        for (int i = 0; i < top.size(); i++) {
            if (i > 0) {
                conclusion.append("；");
            }
            conclusion.append(snippet(top.get(i).getContent(), 160))
                    .append(" [").append(i + 1).append(']');
        }
        conclusion.append("。");
        response.setStatus(READY);
        response.setConclusion(conclusion.toString());
        response.setCitations(buildCitations(List.of(1, 2, 3).subList(0, top.size()), hits));
        applyCitedIndexes(response, response.getCitations(), hits, buildCitationIndex(hits));
        response.setModel("extractive-fallback");
        return finish(response, startedAt);
    }

    private PostAnalysisResponse baseResponse() {
        PostAnalysisResponse response = new PostAnalysisResponse();
        response.setStatus(READY);
        response.setNotice(NOTICE);
        response.setGeneratedAt(LocalDateTime.now().toString());
        return response;
    }

    private PostAnalysisResponse finish(PostAnalysisResponse response, long startedAt) {
        response.setGeneratedAt(LocalDateTime.now().toString());
        response.setLatencyMs((System.nanoTime() - startedAt) / 1_000_000);
        return response;
    }

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

    private String key(long postId, long commentId) {
        return postId + ":" + commentId;
    }

    private String safe(String text) {
        return text == null ? "" : text.strip();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private record ParsedOutput(boolean answerable, String conclusion, List<Integer> cited) {
    }

    private record ConclusionWithCitations(String conclusion, List<AiCitation> citations) {
    }

    public record AnalysisOutcome(PostAnalysisResponse response, List<VectorSearchResult> contextHits) {
    }
}
