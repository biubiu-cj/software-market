package software_market.client.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import software_market.config.AgentServiceProperties;
import software_market.dto.request.ai.PostAnalysisRequestDTO;
import software_market.vo.ai.PostAiAnalysisVO;

import java.net.http.HttpClient;

/**
 * 主应用调用 agent-service 的唯一入口。
 * agent-service 只监听内网，前端不能直接访问。
 */
@Slf4j
@Component
public class AgentClient {

    private final AgentServiceProperties properties;

    private final RestClient restClient;

    public AgentClient(AgentServiceProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 调用失败返回 null，由业务层转换成降级状态；不抛出异常影响帖子详情。
     */
    public PostAiAnalysisVO analyze(PostAnalysisRequestDTO request) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            return null;
        }
        long startedAt = System.nanoTime();
        try {
            PostAiAnalysisVO response = restClient.post()
                    .uri("/internal/agent/post-analysis")
                    .header("X-Internal-Token", properties.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PostAiAnalysisVO.class);
            log.info("agent-service 分析完成: postId={}, status={}, elapsedMs={}",
                    request.getPostId(),
                    response == null ? "NULL" : response.getStatus(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return response;
        } catch (Exception e) {
            log.warn("调用 agent-service 失败: postId={}, elapsedMs={}, error={}",
                    request.getPostId(),
                    (System.nanoTime() - startedAt) / 1_000_000,
                    e.getMessage());
            return null;
        }
    }
}
