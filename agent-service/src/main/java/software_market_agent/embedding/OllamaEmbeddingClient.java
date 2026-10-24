package software_market_agent.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import software_market_agent.config.AgentProperties;
import software_market_agent.exception.RagUnavailableException;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地 Ollama BGE-M3 Embedding 适配层。
 * 接口：POST http://127.0.0.1:11434/api/embed
 * 响应字段：embeddings[0]（不是 OpenAI 的 data[].embedding）
 */
@Slf4j
@Component
public class OllamaEmbeddingClient implements EmbeddingClient {

    private static final int MAX_RETRIES = 3;

    private final AgentProperties properties;

    private final RestClient restClient;

    public OllamaEmbeddingClient(AgentProperties properties) {
        this.properties = properties;
        AgentProperties.Embedding cfg = properties.getEmbedding();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(cfg.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(cfg.getReadTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(cfg.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @PostConstruct
    public void logConfig() {
        AgentProperties.Embedding cfg = properties.getEmbedding();
        log.info("Embedding 使用本地 Ollama: baseUrl={}, model={}, dimension={}",
                cfg.getBaseUrl(), cfg.getModel(), cfg.getDimension());
    }

    @Override
    public float[] embed(String text) {
        List<float[]> result = embedBatch(List.of(text == null ? "" : text));
        return result.get(0);
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<float[]> result = new ArrayList<>(texts.size());
        int batchSize = Math.max(1, properties.getEmbedding().getBatchSize());
        for (int start = 0; start < texts.size(); start += batchSize) {
            int end = Math.min(start + batchSize, texts.size());
            result.addAll(requestBatch(texts.subList(start, end)));
        }
        return result;
    }

    private List<float[]> requestBatch(List<String> inputs) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getEmbedding().getModel());
        body.put("input", inputs);

        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                long startedAt = System.nanoTime();
                JsonNode response = restClient.post()
                        .uri("/api/embed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                List<float[]> vectors = parseResponse(response, inputs.size());
                log.info("Ollama Embedding 完成: model={}, inputCount={}, dimension={}, elapsedMs={}",
                        properties.getEmbedding().getModel(),
                        inputs.size(),
                        vectors.isEmpty() ? 0 : vectors.get(0).length,
                        (System.nanoTime() - startedAt) / 1_000_000);
                return vectors;
            } catch (Exception e) {
                lastError = e;
                log.warn("Ollama Embedding 调用失败: attempt={}/{}, error={}",
                        attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleep(attempt * 500L);
                }
            }
        }
        throw new RagUnavailableException(
                "Ollama Embedding 服务不可用，请确认 ollama serve 已启动且 bge-m3 已下载", lastError);
    }

    private List<float[]> parseResponse(JsonNode response, int expectedCount) {
        if (response == null || !response.path("embeddings").isArray()) {
            throw new RagUnavailableException("Ollama Embedding 响应缺少 embeddings 字段");
        }
        JsonNode embeddings = response.path("embeddings");
        if (embeddings.size() != expectedCount) {
            throw new RagUnavailableException("Ollama Embedding 返回数量不一致: expected="
                    + expectedCount + ", actual=" + embeddings.size());
        }
        int dimension = properties.getEmbedding().getDimension();
        List<float[]> result = new ArrayList<>(expectedCount);
        for (JsonNode item : embeddings) {
            if (!item.isArray() || item.size() != dimension) {
                throw new RagUnavailableException("Ollama Embedding 维度不一致: expected="
                        + dimension + ", actual=" + (item.isArray() ? item.size() : -1));
            }
            float[] vector = new float[dimension];
            for (int i = 0; i < dimension; i++) {
                vector[i] = (float) item.get(i).asDouble();
            }
            result.add(vector);
        }
        return result;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
