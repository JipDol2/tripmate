package com.tripmate.chat;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
        Long id,
        Long postId,
        String postTitle,
        Long hostId,
        int currentParticipants,
        int maxParticipants,
        boolean myCompanionJoined,
        List<ChatParticipantResponse> participants,
        List<String> participantNicknames,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
