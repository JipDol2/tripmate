package com.tripmate.chat;

import com.tripmate.post.CompanionPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByPost(CompanionPost post);

    @Query("select distinct r from ChatRoom r join ChatRoomParticipant p on p.room = r join fetch r.post where p.user.id = :userId and p.kicked = false order by r.updatedAt desc")
    List<ChatRoom> findRoomsByParticipant(@Param("userId") Long userId);
}
