package com.tripmate.review;

import com.tripmate.auth.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/rooms/{roomId}/targets")
    public List<ReviewTargetResponse> targets(@AuthenticationPrincipal CustomUserPrincipal principal,
                                              @PathVariable Long roomId) {
        return reviewService.targets(principal.getUserId(), roomId);
    }

    @PostMapping
    public ReviewResponse create(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(principal.getUserId(), request);
    }
}
