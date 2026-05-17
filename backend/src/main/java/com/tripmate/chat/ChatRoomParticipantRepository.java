package com.tripmate.chat;

import com.tripmate.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    boolean existsByRoomAndUser(ChatRoom room, User user);

    List<ChatRoomParticipant> findByRoom(ChatRoom room);
}
