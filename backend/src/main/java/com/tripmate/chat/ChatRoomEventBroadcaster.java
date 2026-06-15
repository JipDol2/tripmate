package com.tripmate.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatRoomEventBroadcaster {
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    public ChatRoomEventBroadcaster(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void add(Long roomId, WebSocketSession session) {
        sessionsByRoom.computeIfAbsent(roomId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByRoom.remove(roomId);
        }
    }

    public void broadcast(Long roomId, Object event) {
        try {
            broadcastPayload(roomId, objectMapper.writeValueAsString(event));
        } catch (Exception ignored) {
        }
    }

    public void broadcastPayload(Long roomId, String payload) {
        for (WebSocketSession roomSession : sessionsByRoom.getOrDefault(roomId, Set.of())) {
            if (!roomSession.isOpen()) {
                continue;
            }

            try {
                roomSession.sendMessage(new TextMessage(payload));
            } catch (Exception ignored) {
            }
        }
    }
}
