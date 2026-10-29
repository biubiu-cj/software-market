package software_market_agent.eval.service;

import org.junit.jupiter.api.Test;
import software_market_agent.agentscope.ChatModelProvider;
import software_market_agent.config.AgentProperties;
import software_market_agent.eval.loader.EvalCaseLoader;
import software_market_agent.eval.metrics.LlmJudgeService;
import software_market_agent.eval.metrics.RetrievalMetricCalculator;
import software_market_agent.eval.model.EvalBatchReport;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.EvalCaseResult;
import software_market_agent.eval.model.EvalConfig;
import software_market_agent.eval.model.EvalRunRequest;
import software_market_agent.eval.model.GenerationScores;
import software_market_agent.eval.model.RetrievalScores;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagEvalServiceTest {

    @Test
    void aggregatesRetrievalGenerationAndReviewFlags() throws Exception {
        AgentProperties properties = new AgentProperties();
        EvalCaseLoader loader = mock(EvalCaseLoader.class);
        EvalInferenceService inference = mock(EvalInferenceService.class);
        RetrievalMetricCalculator calculator = mock(RetrievalMetricCalculator.class);
        LlmJudgeService judge = mock(LlmJudgeService.class);
        EvalReportStore store = mock(EvalReportStore.class);
        ChatModelProvider provider = mock(ChatModelProvider.class);
        when(provider.isAvailable()).thenReturn(true);

        EvalCase case1 = evalCase("c1");
        EvalCase case2 = evalCase("c2");
        when(loader.load(anyString())).thenReturn(List.of(case1, case2));

        EvalCaseResult ok = result("c1", true, 100L);
        EvalCaseResult failed = result("c2", false, 300L);
        when(inference.infer(eq(case1), any(EvalConfig.class))).thenReturn(ok);
        when(inference.infer(eq(case2), any(EvalConfig.class))).thenReturn(failed);

        when(calculator.calculate(anyList(), eq(case1), eq(8)))
                .thenReturn(new RetrievalScores(true, 0.5, 0.75));
        when(judge.judge(eq(case1), eq(ok), anyBoolean()))
                .thenReturn(generationScores(0.9, 0.4));

        EvalRunRequest request = new EvalRunRequest();
        EvalConfig config = new EvalConfig();
        config.setConcurrency(1);
        request.setConfig(config);

        RagEvalService service = new RagEvalService(
                properties, loader, inference, calculator, judge, store, provider);
        EvalBatchReport report = service.runBatch(request);

        assertEquals(2, report.getTotalCases());
        assertEquals(1, report.getEvaluatedCases());
        assertEquals(1.0, report.getRetrieval().getHitAtK(), 1e-9);
        assertEquals(0.5, report.getRetrieval().getMrr(), 1e-9);
        assertEquals(0.4, report.getGeneration().getRelevancy(), 1e-9);
        assertEquals(200, report.getRetrieval().getAvgLatencyMs());
        assertEquals(100, report.getRetrieval().getP50LatencyMs());
        assertEquals(300, report.getRetrieval().getP95LatencyMs());
        assertEquals(List.of("c2"), report.getFailedCaseIds());
        // c1 相关性低于阈值，c2 推理失败，都进入复核
        assertEquals(new HashSet<>(List.of("c1", "c2")), new HashSet<>(report.getReviewCaseIds()));
        verify(store).save(report);
    }

    private EvalCase evalCase(String id) {
        EvalCase evalCase = new EvalCase();
        evalCase.setId(id);
        evalCase.setQuestion("问题 " + id);
        evalCase.setGroundTruthPostIds(List.of(1L));
        return evalCase;
    }

    private EvalCaseResult result(String caseId, boolean success, long latencyMs) {
        EvalCaseResult result = new EvalCaseResult();
        result.setCaseId(caseId);
        result.setSuccess(success);
        result.setLatencyMs(latencyMs);
        List<VectorSearchResult> hits = new ArrayList<>();
        VectorSearchResult hit = new VectorSearchResult();
        hit.setId("post:1:comment:1:v1");
        hit.setPostId(1);
        hit.setCommentId(1);
        hits.add(hit);
        result.setHits(hits);
        return result;
    }

    private GenerationScores generationScores(double faithfulness, double relevancy) {
        GenerationScores scores = new GenerationScores();
        scores.setFaithfulness(faithfulness);
        scores.setRelevancy(relevancy);
        return scores;
    }
}
