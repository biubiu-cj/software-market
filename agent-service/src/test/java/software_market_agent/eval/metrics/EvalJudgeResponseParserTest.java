package software_market_agent.eval.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvalJudgeResponseParserTest {

    private final EvalJudgeResponseParser parser = new EvalJudgeResponseParser(new ObjectMapper());

    @Test
    void parsesPlainJsonAndClampsScore() {
        EvalJudgeResponseParser.JudgeScore score = parser.parse("{\"score\": 1.4, \"reason\": \"很好\"}");
        assertEquals(1.0, score.score(), 1e-9);
        assertEquals("很好", score.reason());

        assertEquals(0.0, parser.parse("```json\n{\"score\": -0.2}\n```").score(), 1e-9);
    }

    @Test
    void extractsJsonFromExtraText() {
        EvalJudgeResponseParser.JudgeScore score = parser.parse(
                "评估如下：{\"score\": 0.75, \"reason\": \"大部分有依据\"} 完毕");
        assertEquals(0.75, score.score(), 1e-9);
    }

    @Test
    void rejectsMissingScore() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("{\"reason\": \"no score\"}"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("not json"));
    }
}
