package com.tripmate.application;

import com.tripmate.location.LocationService;
import com.tripmate.location.LocationService.ResolvedLocation;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long postId,
        String postTitle,
        String countryCode,
        String countryName,
        String cityCode,
        String city,
        String startDate,
        String endDate,
        Long applicantId,
        String applicantNickname,
        String message,
        ApplicationStatus status,
        LocalDateTime createdAt
) {
    public static ApplicationResponse from(CompanionApplication application, LocationService locationService) {
        ResolvedLocation location = locationService.resolve(application.getPost().getCityCode());

        return new ApplicationResponse(
                application.getId(),
                application.getPost().getId(),
                application.getPost().getTitle(),
                location.countryCode(),
                location.countryName(),
                location.cityCode(),
                location.cityName(),
                application.getPost().getStartDate().toString(),
                application.getPost().getEndDate() == null ? null : application.getPost().getEndDate().toString(),
                application.getApplicant().getId(),
                application.getApplicant().getNickname(),
                application.getMessage(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}
