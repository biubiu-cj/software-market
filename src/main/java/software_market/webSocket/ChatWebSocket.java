package software_market.webSocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software_market.config.security.JwtProvider;
import software_market.dto.request.chat.ChatMessageDTO;
import software_market.entity.ChatMsg;
import software_market.service.ChatService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
@ServerEndpoint("/websocket/auth/chat")
public class ChatWebSocket {

    private static final ConcurrentHashMap<Integer, Set<Session>> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> USER_MAP = new ConcurrentHashMap<>();

    // @ServerEndpoint 实例由 WebSocket 容器按连接创建（非 Spring 管理），
    // 因此不能通过实例字段 @Autowired；由 Spring 管理的单例把依赖写入静态字段供连接实例读取。
    private static JwtProvider jwtProvider;
    private static ChatService chatService;

    @Autowired
    public void setBeanDependencies(JwtProvider jwtProvider, ChatService chatService) {
        ChatWebSocket.jwtProvider = jwtProvider;
        ChatWebSocket.chatService = chatService;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @OnOpen
    public void onOpen(Session session) {
        try {
            String token = extractToken(session.getQueryString());
            if (token == null || token.isEmpty()) {
                sendErrorAndClose(session, "Missing token parameter");
                return;
            }

            // 握手阶段只允许 Access Token；Refresh Token / OAuth2 Token 会被拒绝
            Integer userId = jwtProvider.getUserId(jwtProvider.parseAccessToken(token));
            if (userId == null) {
                sendErrorAndClose(session, "Invalid token");
                return;
            }

            SESSION_MAP.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
            USER_MAP.put(session, userId);
            log.info("用户{} websocket连接成功，当前该用户共{}个连接", userId, SESSION_MAP.get(userId).size());
        } catch (Exception e) {
            log.error("websocket连接失败", e);
            sendErrorAndClose(session, "Authentication failed");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳消息直接跳过，不做任何解析
        if (message.contains("\"beat\"")) {
            return;
        }

        try {
            Integer senderUserId = USER_MAP.get(session);
            if (senderUserId == null) {
                return;
            }

            ChatMessageDTO dto = JSON.parseObject(message, ChatMessageDTO.class);
            log.info("用户{} 给用户{} 发送消息: {}", senderUserId, dto.getTargetUserId(), dto.getContent());

            LocalDateTime now = LocalDateTime.now();
            ChatMsg msg = ChatMsg.builder()
                    .senderUserId(senderUserId)
                    .targetUserId(dto.getTargetUserId())
                    .content(dto.getContent())
                    .isAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() ? 1 : 0)
                    .createdAt(now)
                    .isRead(0)
                    .build();
            chatService.saveMessage(msg);

            String responseJson = buildMessageJson(msg, now);

            // 转发给目标用户的所有 session
            sendToUser(dto.getTargetUserId(), responseJson);

            // 同步给发送者自己的其他 session（多 tab 场景）
            sendToUserExcept(senderUserId, session, responseJson);
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer userId = USER_MAP.remove(session);
        if (userId != null) {
            SESSION_MAP.computeIfPresent(userId, (k, sessions) -> {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    log.info("用户{} websocket全部断开", userId);
                    return null;
                }
                log.info("用户{} websocket断开一个连接，还剩{}个", userId, sessions.size());
                return sessions;
            });
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        Integer userId = USER_MAP.get(session);
        log.error("用户{} websocket异常", userId, error);
    }

    private String extractToken(String queryString) {
        if (queryString == null) {
            return null;
        }
        try {
            for (String param : queryString.split("&")) {
                if (param.startsWith("token=")) {
                    return URLDecoder.decode(param.substring(6), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("解析token参数失败", e);
        }
        return null;
    }

    private void sendErrorAndClose(Session session, String errorMsg) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("{\"error\":\"" + errorMsg + "\"}");
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭异常session失败", e);
        }
    }

    private String buildMessageJson(ChatMsg msg, LocalDateTime now) {
        JSONObject response = new JSONObject();
        response.put("chatMsgID", msg.getChatMsgId());
        response.put("senderUserID", msg.getSenderUserId());
        response.put("targetUserID", msg.getTargetUserId());
        response.put("content", msg.getContent());
        response.put("createdAt", (msg.getCreatedAt() != null ? msg.getCreatedAt() : now).format(FORMATTER));
        response.put("isAnonymous", msg.getIsAnonymous() != null && msg.getIsAnonymous() == 1);
        return response.toString();
    }

    private void sendToUser(Integer userId, String json) {
        Set<Session> sessions = SESSION_MAP.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("用户{}不在线，消息未送达。当前在线用户: {}", userId, SESSION_MAP.keySet());
            return;
        }
        log.info("向用户{}推送消息，共{}个session", userId, sessions.size());
        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(json);
                    log.info("消息已发送至用户{}的session", userId);
                } catch (Exception e) {
                    log.error("向用户{}发送消息失败", userId, e);
                }
            }
        }
    }

    private void sendToUserExcept(Integer userId, Session except, String json) {
        Set<Session> sessions = SESSION_MAP.get(userId);
        if (sessions != null) {
            for (Session s : sessions) {
                if (s != except && s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(json);
                    } catch (Exception e) {
                        log.error("向用户{}同步消息失败", userId, e);
                    }
                }
            }
        }
    }
}
