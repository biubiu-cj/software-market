package software_market_agent.eval.metrics;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software_market_agent.agentscope.ChatModelProvider;
import software_market_agent.agentscope.PromptLoader;
import software_market_agent.config.AgentProperties;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.EvalCaseResult;
import software_market_agent.eval.model.GenerationScores;
import software_market_agent.service.PostAnalysisService;

import java.util.List;
import java.util.Map;

/**
 * LLM-as-Judge：忠实度、相关性、答案正确性、上下文精确率与召回率。
 * 每项独立容错，单项失败不影响其他指标。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmJudgeService {

    private static final String JUDGE_SYSTEM_PROMPT =
            "你是严格的 RAG 质量评估员。只输出一个 JSON 对象，不要输出 Markdown 代码块或解释文字。";
    private static final int MAX_CONTEXT_DOCS = 5;
    private static final int MAX_CONTEXT_CHARS = 1200;

    private final AgentProperties properties;
    private final ChatModelProvider chatModelProvider;
    private final PromptLoader promptLoader;
    private final EvalJudgeResponseParser responseParser;

    public GenerationScores judge(EvalCase evalCase, EvalCaseResult caseResult, boolean answerEvaluated) {
        GenerationScores scores = new GenerationScores();
        if (!chatModelProvider.isAvailable()) {
            return scores;
        }
        String question = evalCase.getQuestion();
        String contexts = formatContexts(caseResult.getContexts());

        if (StringUtils.hasText(contexts)) {
            try {
                EvalJudgeResponseParser.JudgeScore precision = judge(
                        "prompts/eval/context-precision.txt",
                        Map.of("question", question, "contexts", contexts));
                scores.setContextPrecision(precision.score());
                scores.setContextPrecisionReason(precision.reason());
            } catch (Exception e) {
                log.warn("Context Precision 评分失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
            }
            if (evalCase.hasAnswerGroundTruth()) {
                try {
                    EvalJudgeResponseParser.JudgeScore recall = judge(
                            "prompts/eval/context-recall.txt",
                            Map.of(
                                    "question", question,
                                    "ground_truth_answer", evalCase.getGroundTruthAnswer(),
                                    "contexts", contexts));
                    scores.setContextRecall(recall.score());
                    scores.setContextRecallReason(recall.reason());
                } catch (Exception e) {
                    log.warn("Context Recall 评分失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
                }
            }
        }

        if (!answerEvaluated || !StringUtils.hasText(caseResult.getAnswer())
                || !PostAnalysisService.READY.equals(caseResult.getStatus())) {
            return scores;
        }
        String answer = caseResult.getAnswer();
        if (StringUtils.hasText(contexts)) {
            try {
                EvalJudgeResponseParser.JudgeScore faithfulness = judge(
                        "prompts/eval/faithfulness.txt",
                        Map.of("question", question, "contexts", contexts, "answer", answer));
                scores.setFaithfulness(faithfulness.score());
                scores.setFaithfulnessReason(faithfulness.reason());
            } catch (Exception e) {
                log.warn("Faithfulness 评分失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
            }
        }
        try {
            EvalJudgeResponseParser.JudgeScore relevancy = judge(
                    "prompts/eval/relevancy.txt",
                    Map.of("question", question, "answer", answer));
            scores.setRelevancy(relevancy.score());
            scores.setRelevancyReason(relevancy.reason());
        } catch (Exception e) {
            log.warn("Relevancy 评分失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
        }
        if (evalCase.hasAnswerGroundTruth()) {
            try {
                EvalJudgeResponseParser.JudgeScore correctness = judge(
                        "prompts/eval/correctness.txt",
                        Map.of(
                                "question", question,
                                "ground_truth_answer", evalCase.getGroundTruthAnswer(),
                                "answer", answer));
                scores.setAnswerCorrectness(correctness.score());
                scores.setAnswerCorrectnessReason(correctness.reason());
            } catch (Exception e) {
                log.warn("Answer Correctness 评分失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
            }
        }
        return scores;
    }

    private EvalJudgeResponseParser.JudgeScore judge(String promptPath, Map<String, String> variables) {
        String prompt = render(promptLoader.load(promptPath), variables);
        try (ReActAgent agent = ReActAgent.builder()
                .name("rag-eval-judge")
                .model(chatModelProvider.getModel())
                .sysPrompt(JUDGE_SYSTEM_PROMPT)
                .maxIters(1)
                .build()) {
            RuntimeContext context = RuntimeContext.builder()
                    .sessionId("rag-eval-judge")
                    .userId("system")
                    .build();
            Msg message = agent.call(prompt, context).block(properties.getChat().getTimeout());
            if (message == null || !StringUtils.hasText(message.getTextContent())) {
                throw new IllegalStateException("Judge 返回为空");
            }
            return responseParser.parse(message.getTextContent());
        }
    }

    private String render(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private String formatContexts(List<String> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String context : contexts) {
            if (count >= MAX_CONTEXT_DOCS) {
                break;
            }
            if (!StringUtils.hasText(context)) {
                continue;
            }
            count++;
            builder.append("[片段").append(count).append("]\n")
                    .append(truncate(context, MAX_CONTEXT_CHARS))
                    .append("\n\n");
        }
        return builder.toString().strip();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
