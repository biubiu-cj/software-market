package software_market.webSocket;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知实时推送。
 *
 * 数据库是通知的事实来源，这里只在通知落库后推一个轻量信号，
 * 前端收到后重新拉取未读数/列表。推送失败只记日志，不影响业务主流程。
 */
@Slf4j
@Component
public class NoticePushService {

    public void pushNewNotice(Integer receiver, Integer noticeId) {
        if (receiver == null) {
            return;
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("event", "NewNotice");
            payload.put("noticeID", noticeId);
            ChatWebSocket.pushToUser(receiver, payload.toString());
        } catch (Exception e) {
            log.warn("通知实时推送失败, receiver={}, noticeId={}", receiver, noticeId, e);
        }
    }
}