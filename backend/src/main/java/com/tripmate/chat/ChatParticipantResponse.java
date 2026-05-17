package com.tripmate.chat;

public record ChatParticipantResponse(
        Long id,
        Long userId,
        String nickname,
        boolean host,
        boolean companionJoined
) {
    public static ChatParticipantResponse from(ChatRoomParticipant participant) {
        Long hostId = participant.getRoom().getPost().getAuthor().getId();

        return new ChatParticipantResponse(
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getNickname(),
                participant.getUser().getId().equals(hostId),
                participant.isCompanionJoined()
        );
    }
}
