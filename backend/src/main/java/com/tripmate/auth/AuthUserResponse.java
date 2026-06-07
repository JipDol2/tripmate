package com.tripmate.auth;

import com.tripmate.user.Gender;
import com.tripmate.user.User;

public record AuthUserResponse(
        Long id,
        String email,
        String nickname,
        String ageRange,
        Gender gender,
        String bio,
        String profileImageUrl
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAgeRange(),
                user.getGender(),
                user.getBio(),
                user.getProfileImageUrl()
        );
    }
}
