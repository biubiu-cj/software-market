package software_market_agent.embedding;

import java.util.List;

public interface EmbeddingClient {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);
}
