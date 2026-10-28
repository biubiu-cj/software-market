package software_market_agent.exception;

/**
 * Milvus / Embedding / 检索链路不可用。
 */
public class RagUnavailableException extends RuntimeException {

    public RagUnavailableException(String message) {
        super(message);
    }

    public RagUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
