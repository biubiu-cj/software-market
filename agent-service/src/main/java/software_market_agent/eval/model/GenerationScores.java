package software_market_agent.eval.model;

import lombok.Data;

@Data
public class GenerationScores {

    private Double faithfulness;
    private String faithfulnessReason;
    private Double relevancy;
    private String relevancyReason;
    private Double answerCorrectness;
    private String answerCorrectnessReason;
    private Double contextPrecision;
    private String contextPrecisionReason;
    private Double contextRecall;
    private String contextRecallReason;
}
