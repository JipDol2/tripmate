package com.tripmate.post;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        @NotBlank String cityCode,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String timeSlot,
        @NotEmpty List<CompanionPurpose> purposes,
        @Min(1) @Max(10) int maxParticipants,
        String genderPreference,
        List<String> agePreferences,
        @NotBlank String title,
        @NotBlank String content,
        List<String> travelStyles
) {}
