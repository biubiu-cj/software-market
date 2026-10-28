package software_market_agent.exception;

/**
 * 大模型调用失败、超时或返回非法 JSON。
 */
public class ModelCallException extends RuntimeException {

    public ModelCallException(String message) {
        super(message);
    }

    public ModelCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
