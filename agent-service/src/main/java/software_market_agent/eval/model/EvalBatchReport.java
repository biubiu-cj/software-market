package software_market_agent.eval.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EvalBatchReport {

    private String runId;
    private String datasetPath;
    private EvalConfig config;
    private int totalCases;
    private int evaluatedCases;
    private int generationEvaluatedCases;
    private boolean judgeEnabled;
    private String createdAt;
    private List<String> failedCaseIds = new ArrayList<>();
    private List<String> reviewCaseIds = new ArrayList<>();
    private RetrievalMetricsSummary retrieval = new RetrievalMetricsSummary();
    private GenerationMetricsSummary generation = new GenerationMetricsSummary();
    private ContextMetricsSummary context = new ContextMetricsSummary();
    private List<EvalCaseResult> caseResults = new ArrayList<>();

    @Data
    public static class RetrievalMetricsSummary {
        private boolean present;
        private Double hitAtK;
        private Double mrr;
        private Double recallAtK;
        private long avgLatencyMs;
        private long p50LatencyMs;
        private long p95LatencyMs;
    }

    @Data
    public static class GenerationMetricsSummary {
        private int faithfulnessCount;
        private int relevancyCount;
        private int answerCorrectnessCount;
        private Double faithfulness;
        private Double relevancy;
        private Double answerCorrectness;
    }

    @Data
    public static class ContextMetricsSummary {
        private int contextPrecisionCount;
        private int contextRecallCount;
        private Double contextPrecision;
        private Double contextRecall;
    }
}
