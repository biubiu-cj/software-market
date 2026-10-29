package software_market_agent.eval.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 解析 Judge 返回的 {"score": 0.0-1.0, "reason": "..."}。
 */
@Component
@RequiredArgsConstructor
public class EvalJudgeResponseParser {

    private final ObjectMapper objectMapper;

    public JudgeScore parse(String content) {
        String cleaned = content == null ? "" : content
                .replace("```json", "")
                .replace("```", "")
                .strip();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Judge 未返回 JSON: " + abbreviate(cleaned));
        }
        try {
            JsonNode root = objectMapper.readTree(cleaned.substring(start, end + 1));
            JsonNode scoreNode = root.path("score");
            if (!scoreNode.isNumber()) {
                throw new IllegalArgumentException("Judge 返回缺少 score");
            }
            double score = Math.max(0.0, Math.min(1.0, scoreNode.asDouble()));
            String reason = root.path("reason").asText("");
            return new JudgeScore(score, truncate(reason, 300));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("解析 Judge JSON 失败: " + e.getMessage(), e);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String abbreviate(String text) {
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }

    public record JudgeScore(double score, String reason) {
    }
}
