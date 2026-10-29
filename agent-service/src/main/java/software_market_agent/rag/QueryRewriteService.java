package software_market_agent.rag;

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

/**
 * 检索前改写查询。改写只影响召回，不影响展示给用户的原文；
 * 模型未配置、调用失败或输出不合法时返回 null，调用方退回原查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryRewriteService {

    private static final String PROMPT_PATH = "prompts/query-rewrite.txt";
    private static final int MAX_INPUT_CHARS = 1500;

    private final AgentProperties properties;
    private final ChatModelProvider chatModelProvider;
    private final PromptLoader promptLoader;

    public String rewrite(String title, String content) {
        if (!properties.getRag().isRewriteEnabled() || !chatModelProvider.isAvailable()) {
            return null;
        }
        String input = buildInput(title, content);
        if (!StringUtils.hasText(input)) {
            return null;
        }
        long startedAt = System.nanoTime();
        try (ReActAgent agent = ReActAgent.builder()
                .name("query-rewrite-agent")
                .model(chatModelProvider.getModel())
                .sysPrompt(promptLoader.load(PROMPT_PATH))
                .maxIters(1)
                .build()) {
            RuntimeContext context = RuntimeContext.builder()
                    .sessionId("query-rewrite")
                    .userId("system")
                    .build();
            Msg message = agent.call(input, context).block(properties.getRag().getRewriteTimeout());
            String rewritten = normalize(message == null ? null : message.getTextContent());
            if (rewritten == null) {
                log.warn("查询改写结果为空，使用原查询");
                return null;
            }
            log.info("查询改写完成: elapsedMs={}, rewritten={}",
                    (System.nanoTime() - startedAt) / 1_000_000, rewritten);
            return rewritten;
        } catch (Exception e) {
            log.warn("查询改写失败，使用原查询: {}", e.getMessage());
            return null;
        }
    }

    private String buildInput(String title, String content) {
        String safeTitle = title == null ? "" : title.strip();
        String safeContent = content == null ? "" : content.strip();
        if (safeContent.length() > MAX_INPUT_CHARS) {
            safeContent = safeContent.substring(0, MAX_INPUT_CHARS);
        }
        return "标题：" + safeTitle + "\n正文：" + safeContent;
    }

    String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String text = null;
        for (String line : raw.replace("\r", "\n").split("\n")) {
            String trimmed = line.strip();
            boolean fenceLine = trimmed.startsWith("```");
            String candidate = trimmed.replace("```", "").strip();
            if (fenceLine) {
                // 跳过 ```json / ```text 这类语言标记行
                candidate = candidate.replaceFirst("^[a-zA-Z]{1,10}\\s+", "");
                if (candidate.matches("[a-zA-Z]{1,10}")) {
                    continue;
                }
            }
            if (!candidate.isEmpty()) {
                text = candidate;
                break;
            }
        }
        if (text == null) {
            return null;
        }
        text = text.replaceAll("^[\\s\"'“”‘’「」]+|[\\s\"'“”‘’「」]+$", "").strip();
        for (String prefix : new String[]{"查询：", "输出：", "改写结果："}) {
            if (text.startsWith(prefix)) {
                text = text.substring(prefix.length()).strip();
            }
        }
        if (!StringUtils.hasText(text)) {
            return null;
        }
        int maxChars = Math.max(10, properties.getRag().getRewriteMaxChars());
        if (text.length() > maxChars) {
            text = text.substring(0, maxChars);
        }
        return text;
    }
}
