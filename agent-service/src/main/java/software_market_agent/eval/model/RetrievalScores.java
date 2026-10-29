package software_market_agent.eval.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalScores {

    private boolean hitAtK;
    private double mrr;
    private double recallAtK;
}
