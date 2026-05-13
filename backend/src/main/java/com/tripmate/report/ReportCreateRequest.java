package com.tripmate.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull Long targetUserId,
        Long targetPostId,
        @NotBlank String reason,
        String detail
) {}
