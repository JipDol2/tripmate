package com.tripmate.user;

import com.tripmate.post.PostResponse;
import com.tripmate.review.ReviewResponse;
import java.time.LocalDateTime;
import java.util.List;

public record PublicUserProfileResponse(
        Long id,
        String nickname,
        String ageRange,
        Gender gender,
        String bio,
        String profileImageUrl,
        List<String> travelStyles,
        List<String> languages,
        LocalDateTime joinedAt,
        List<PostResponse> posts,
        List<ReviewResponse> reviews
) {
    public static PublicUserProfileResponse from(User user, List<PostResponse> posts, List<ReviewResponse> reviews) {
        return new PublicUserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getAgeRange(),
                user.getGender(),
                user.getBio(),
                user.getProfileImageUrl(),
                List.copyOf(user.getTravelStyles()),
                List.copyOf(user.getLanguages()),
                user.getCreatedAt(),
                posts,
                reviews
        );
    }
}
