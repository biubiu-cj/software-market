package software_market_agent.eval.model;

import lombok.Data;

@Data
public class EvalConfig {

    /** 0 表示使用 agent.rag.top-k */
    private int topK = 0;
    private int concurrency = 2;
    private boolean enableGeneration = true;
    private boolean enableLlmJudge = true;
    /** 0 表示全部用例 */
    private int maxCases = 0;
    private double reviewScoreThreshold = 0.6;
    private double reviewMrrThreshold = 0.5;
    private double reviewRecallAtKThreshold = 0.5;
}
