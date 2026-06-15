package com.tripmate.review;

public record ReviewTargetResponse(
        Long userId,
        String nickname,
        boolean reviewed
) {
}
