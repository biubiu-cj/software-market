package software_market_agent.embedding;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software_market_agent.config.AgentProperties;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OllamaEmbeddingClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void parsesOllamaEmbeddingsResponse() throws Exception {
        int dimension = 1024;
        StringBuilder vector = new StringBuilder("[");
        for (int i = 0; i < dimension; i++) {
            if (i > 0) {
                vector.append(',');
            }
            vector.append(i == 0 ? "0.1" : "0.0");
        }
        vector.append(']');
        String response = "{\"model\":\"bge-m3\",\"embeddings\":[" + vector + "]}";

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/embed", exchange -> {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        AgentProperties properties = new AgentProperties();
        properties.getEmbedding().setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.getEmbedding().setDimension(dimension);
        properties.getEmbedding().setBatchSize(16);

        OllamaEmbeddingClient client = new OllamaEmbeddingClient(properties);
        float[] embedding = client.embed("如何解决数据库连接超时问题？");

        assertEquals(dimension, embedding.length);
        assertEquals(0.1f, embedding[0], 0.0001);
    }
}
