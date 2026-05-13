package com.tripmate.post;

import com.tripmate.location.LocationService;
import com.tripmate.location.LocationService.ResolvedLocation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record PostResponse(
        Long id,
        Long authorId,
        String authorNickname,
        String authorAgeRange,
        String authorGender,
        String countryCode,
        String countryName,
        String cityCode,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        String timeSlot,
        List<CompanionPurpose> purposes,
        int maxParticipants,
        String genderPreference,
        String title,
        String content,
        List<String> travelStyles,
        PostStatus status,
        LocalDateTime createdAt
) {
    public static PostResponse from(CompanionPost post, LocationService locationService) {
        ResolvedLocation location = locationService.resolve(post.getCityCode());

        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getAuthor().getAgeRange(),
                post.getAuthor().getGender().name(),
                location.countryCode(),
                location.countryName(),
                location.cityCode(),
                location.cityName(),
                post.getStartDate(),
                post.getEndDate(),
                post.getTimeSlot(),
                List.copyOf(post.getPurposes()),
                post.getMaxParticipants(),
                post.getGenderPreference(),
                post.getTitle(),
                post.getContent(),
                post.getTravelStyles().stream()
                        .filter(Objects::nonNull)
                        .toList(),
                post.getStatus(),
                post.getCreatedAt()
        );
    }
}
