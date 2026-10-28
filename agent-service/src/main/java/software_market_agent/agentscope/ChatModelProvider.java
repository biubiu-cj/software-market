package software_market_agent.agentscope;

import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.compat.deepseek.DeepSeekFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software_market_agent.config.AgentProperties;

/**
 * 只有配置了 DeepSeek API Key 时才创建 AgentScope Model。
 * 未配置时由 PostAnalysisService 走检索摘要兜底，保证链路可演示。
 */
@Slf4j
@Component
public class ChatModelProvider {

    private final Model model;

    public ChatModelProvider(AgentProperties properties) {
        AgentProperties.Chat chat = properties.getChat();
        if (!StringUtils.hasText(chat.getApiKey())) {
            this.model = null;
            log.warn("未配置 DEEPSEEK_API_KEY，AI 分析将使用检索摘要兜底，不调用大模型。");
            return;
        }
        this.model = OpenAIChatModel.builder()
                .apiKey(chat.getApiKey())
                .baseUrl(chat.getBaseUrl())
                .modelName(chat.getModel())
                .formatter(new DeepSeekFormatter())
                .build();
        log.info("AgentScope 模型初始化完成: baseUrl={}, model={}", chat.getBaseUrl(), chat.getModel());
    }

    public Model getModel() {
        return model;
    }

    public boolean isAvailable() {
        return model != null;
    }
}
