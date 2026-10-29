package software_market_agent.eval.model;

import java.util.List;

public record EvalRunPage(int page, int size, long total, List<EvalRunSummary> items) {
}
