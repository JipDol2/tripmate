package com.tripmate.chat;

import com.tripmate.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomOrderByCreatedAtAsc(ChatRoom room);

    Optional<ChatMessage> findTopByRoomOrderByCreatedAtDesc(ChatRoom room);

    long countByRoomAndSenderNot(ChatRoom room, User sender);

    long countByRoomAndSenderNotAndCreatedAtAfter(ChatRoom room, User sender, LocalDateTime createdAt);
}
