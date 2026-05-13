package com.tripmate.application;

import jakarta.validation.constraints.NotBlank;

public record ApplicationCreateRequest(@NotBlank String message) {}
