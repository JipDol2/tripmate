package com.tripmate.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long reviewerId,
        String reviewerNickname,
        Long postId,
        String postTitle,
        int rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(CompanionReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewer().getNickname(),
                review.getPost().getId(),
                review.getPost().getTitle(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
