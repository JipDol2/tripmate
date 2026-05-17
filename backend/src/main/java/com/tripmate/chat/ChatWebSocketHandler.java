package com.tripmate.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.auth.AuthTokenService;
import com.tripmate.auth.JwtTokenProvider;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ChatService chatService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthTokenService authTokenService;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService,
                                JwtTokenProvider jwtTokenProvider,
                                AuthTokenService authTokenService,
                                ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authTokenService = authTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getLongQueryParam(session.getUri(), "roomId");
        String token = getQueryParam(session.getUri(), "token");

        if (roomId == null || token == null || !authTokenService.isTokenActive(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        Long userId = jwtTokenProvider.getUserId(token);

        if (!chatService.isParticipant(userId, roomId)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        session.getAttributes().put("roomId", roomId);
        session.getAttributes().put("userId", userId);
        sessionsByRoom.computeIfAbsent(roomId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");

        if (roomId == null || userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        ChatMessageRequest request = objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);
        ChatMessageResponse response;

        try {
            response = chatService.sendMessage(userId, roomId, request.content());
        } catch (Exception ignored) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String payload = objectMapper.writeValueAsString(response);

        for (WebSocketSession roomSession : sessionsByRoom.getOrDefault(roomId, Set.of())) {
            if (roomSession.isOpen()) {
                roomSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long roomId = (Long) session.getAttributes().get("roomId");
        if (roomId == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByRoom.remove(roomId);
            }
        }
    }

    private Long getLongQueryParam(URI uri, String key) {
        String value = getQueryParam(uri, key);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String getQueryParam(URI uri, String key) {
        if (uri == null || uri.getRawQuery() == null) {
            return null;
        }

        for (String pair : uri.getRawQuery().split("&")) {
            String[] parts = pair.split("=", 2);
            String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (!key.equals(name)) {
                continue;
            }

            return parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
        }

        return null;
    }
}
