package com.tripmate.chat;

public record ChatRoomLeaveEvent(
        String type,
        Long roomId,
        Long userId,
        String nickname,
        int remainingParticipants
) {
    public static ChatRoomLeaveEvent of(Long roomId, Long userId, String nickname, int remainingParticipants) {
        return new ChatRoomLeaveEvent("LEAVE", roomId, userId, nickname, remainingParticipants);
    }
}
