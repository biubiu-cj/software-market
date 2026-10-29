package software_market_agent.eval.model;

import lombok.Data;

@Data
public class EvalRunSummary {

    private String runId;
    private String datasetPath;
    private int totalCases;
    private String createdAt;
    private Double hitAtK;
    private Double mrr;
    private Double recallAtK;
    private Double faithfulness;
    private Double relevancy;
    private Double answerCorrectness;
    private Double contextPrecision;
    private Double contextRecall;
    private Long avgLatencyMs;
}
