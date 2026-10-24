package software_market.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 业务代码只发布 Spring 事件，真正的 Kafka 发送在事务提交后执行。
 */
@Component
@RequiredArgsConstructor
public class RagIndexEventPublisher {

    public static final String POST_REINDEX = "POST_REINDEX";
    public static final String POST_DELETE = "POST_DELETE";

    private final ApplicationEventPublisher publisher;

    public void reindex(Integer postId) {
        if (postId == null) {
            return;
        }
        publisher.publishEvent(new RagIndexRequestedEvent(postId, POST_REINDEX, System.currentTimeMillis()));
    }

    public void delete(Integer postId) {
        if (postId == null) {
            return;
        }
        publisher.publishEvent(new RagIndexRequestedEvent(postId, POST_DELETE, System.currentTimeMillis()));
    }
}
