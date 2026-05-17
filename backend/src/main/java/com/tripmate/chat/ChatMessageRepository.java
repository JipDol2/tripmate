package com.tripmate.chat;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomOrderByCreatedAtAsc(ChatRoom room);

    Optional<ChatMessage> findTopByRoomOrderByCreatedAtDesc(ChatRoom room);
}
