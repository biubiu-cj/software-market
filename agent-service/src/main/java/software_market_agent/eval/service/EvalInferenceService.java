package software_market_agent.eval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.EvalCaseResult;
import software_market_agent.eval.model.EvalConfig;
import software_market_agent.model.internal.PostAnalysisRequest;
import software_market_agent.model.milvus.VectorSearchResult;
import software_market_agent.rag.RagRetrievalService;
import software_market_agent.service.PostAnalysisService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 单条评测用例推理：复用线上检索与生成链路，保证评测结果可迁移。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvalInferenceService {

    private static final int MAX_CONTEXT_CHARS = 1200;

    private final PostAnalysisService postAnalysisService;

    public EvalCaseResult infer(EvalCase evalCase, EvalConfig config) {
        long startedAt = System.nanoTime();
        EvalCaseResult result = new EvalCaseResult();
        result.setCaseId(evalCase.getId());
        result.setQuestion(evalCase.getQuestion());

        PostAnalysisRequest request = new PostAnalysisRequest();
        request.setTitle(evalCase.getQuestion());
        request.setContent("");
        request.setCategory(evalCase.getCategory());

        try {
            request.setPostId(evalCase.getExcludePostId() == null
                    ? 0
                    : Math.toIntExact(evalCase.getExcludePostId()));
            RagRetrievalService.RetrievalResult retrieval = postAnalysisService.prepare(request);
            result.setHits(new ArrayList<>(retrieval.hits()));
            result.setHitCount(retrieval.hits().size());
            result.setRetrievedPostIds(distinctPostIds(retrieval.hits()));
            result.setRetrievedTitles(distinctTitles(retrieval.hits()));
            result.setContexts(toContexts(dedupeByComment(retrieval.hits())));

            if (config.isEnableGeneration()) {
                PostAnalysisService.AnalysisOutcome outcome =
                        postAnalysisService.analyzeWithRetrieval(request, retrieval);
                result.setStatus(outcome.response().getStatus());
                result.setAnswer(outcome.response().getConclusion());
                result.setContexts(toContexts(outcome.contextHits()));
            }
            result.setSuccess(true);
        } catch (Exception e) {
            log.warn("评测用例推理失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
            result.setSuccess(false);
            result.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
        result.setLatencyMs((System.nanoTime() - startedAt) / 1_000_000);
        return result;
    }

    private List<VectorSearchResult> dedupeByComment(List<VectorSearchResult> hits) {
        Map<String, VectorSearchResult> unique = new LinkedHashMap<>();
        for (VectorSearchResult hit : hits) {
            unique.putIfAbsent(hit.getPostId() + ":" + hit.getCommentId(), hit);
        }
        return new ArrayList<>(unique.values());
    }

    private List<Long> distinctPostIds(List<VectorSearchResult> hits) {
        Set<Long> ids = new LinkedHashSet<>();
        for (VectorSearchResult hit : hits) {
            ids.add(hit.getPostId());
        }
        return new ArrayList<>(ids);
    }

    private List<String> distinctTitles(List<VectorSearchResult> hits) {
        Set<String> titles = new LinkedHashSet<>();
        for (VectorSearchResult hit : hits) {
            if (hit.getTitle() != null && !hit.getTitle().isBlank()) {
                titles.add(hit.getTitle().strip());
            }
        }
        return new ArrayList<>(titles);
    }

    private List<String> toContexts(List<VectorSearchResult> hits) {
        List<String> contexts = new ArrayList<>();
        for (VectorSearchResult hit : hits) {
            String content = hit.getContent();
            if (content == null || content.isBlank()) {
                continue;
            }
            contexts.add(content.length() <= MAX_CONTEXT_CHARS
                    ? content
                    : content.substring(0, MAX_CONTEXT_CHARS));
        }
        return contexts;
    }
}
