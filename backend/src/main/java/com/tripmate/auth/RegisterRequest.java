package com.tripmate.auth;

import com.tripmate.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nickname,
        String ageRange,
        Gender gender
) {}
