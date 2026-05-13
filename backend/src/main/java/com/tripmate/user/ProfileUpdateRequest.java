package com.tripmate.user;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProfileUpdateRequest(
        @NotBlank String nickname,
        String ageRange,
        Gender gender,
        String bio,
        String profileImageUrl,
        List<String> travelStyles,
        List<String> languages
) {}
