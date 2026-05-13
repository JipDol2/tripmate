package com.tripmate.user;

import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String ageRange,
        Gender gender,
        String bio,
        String profileImageUrl,
        List<String> travelStyles,
        List<String> languages
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAgeRange(),
                user.getGender(),
                user.getBio(),
                user.getProfileImageUrl(),
                List.copyOf(user.getTravelStyles()),
                List.copyOf(user.getLanguages())
        );
    }
}
