package software_market_agent.agentscope;

import io.agentscope.core.ReActAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostAnalysisAgentBuilder {

    private final ChatModelProvider chatModelProvider;

    private final PromptLoader promptLoader;

    public ReActAgent build() {
        if (!chatModelProvider.isAvailable()) {
            throw new IllegalStateException("Chat model is not configured");
        }
        return ReActAgent.builder()
                .name("post-analysis-agent")
                .model(chatModelProvider.getModel())
                .sysPrompt(promptLoader.load("prompts/post-analysis.txt"))
                .maxIters(1)
                .build();
    }
}
