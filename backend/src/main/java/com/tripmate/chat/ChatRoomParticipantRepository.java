package com.tripmate.chat;

import com.tripmate.post.CompanionPost;
import com.tripmate.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    boolean existsByRoomAndUser(ChatRoom room, User user);

    Optional<ChatRoomParticipant> findByRoomAndUser(ChatRoom room, User user);

    List<ChatRoomParticipant> findByRoomAndKickedFalse(ChatRoom room);

    List<ChatRoomParticipant> findByRoomAndCompanionJoinedTrueAndKickedFalse(ChatRoom room);

    @Query("select count(distinct p.user.id) from ChatRoomParticipant p where p.room.post = :post and p.companionJoined = true and p.kicked = false")
    long countJoinedParticipantsByPost(@Param("post") CompanionPost post);
}
