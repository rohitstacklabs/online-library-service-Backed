package com.online_library_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("User {} connected via WebSocket", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("User {} disconnected from WebSocket", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String userIdParam = session.getUri().getQuery().split("=")[1];
            return Long.parseLong(userIdParam);
        } catch (Exception e) {
            log.warn("Failed to extract userId from WebSocket session: {}", e.getMessage());
            return null;
        }
    }

    public void sendNotificationToUser(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("Sent WebSocket notification to user {}: {}", userId, message);
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage());
            }
        }
    }
}
