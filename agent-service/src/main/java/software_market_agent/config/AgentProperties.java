package software_market_agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    private String internalToken = "dev-token";

    private Rag rag = new Rag();

    private Embedding embedding = new Embedding();

    private Chat chat = new Chat();

    private Milvus milvus = new Milvus();

    @Data
    public static class Rag {
        private String collection = "software_market_rag";
        private int topK = 8;
        private double scoreThreshold = 0.55;
        private int maxRelatedPosts = 5;
        private int maxRelatedReplies = 8;
        private int maxRepliesPerPost = 2;
        private int minCommentLength = 8;
        private int chunkSize = 600;
        private int chunkMaxSize = 800;
        private int chunkOverlap = 80;
        private int upsertBatchSize = 20;
    }

    @Data
    public static class Embedding {
        private String baseUrl = "http://127.0.0.1:11434";
        private String model = "bge-m3";
        private int dimension = 1024;
        private int batchSize = 16;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(60);
    }

    @Data
    public static class Chat {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey = "";
        private String model = "deepseek-chat";
        private Duration timeout = Duration.ofSeconds(60);
    }

    @Data
    public static class Milvus {
        private String host = "localhost";
        private int port = 19530;
        private String collection = "software_market_rag";
        private Duration connectTimeout = Duration.ofSeconds(5);
        private boolean ensureOnStartup = true;
    }
}
