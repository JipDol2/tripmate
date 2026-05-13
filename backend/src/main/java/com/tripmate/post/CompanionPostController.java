package com.tripmate.post;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.common.ApiException;
import com.tripmate.location.LocationService;
import com.tripmate.user.UserRepository;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class CompanionPostController {
    private final CompanionPostRepository companionPostRepository;
    private final LocationService locationService;
    private final UserRepository userRepository;

    public CompanionPostController(CompanionPostRepository companionPostRepository, LocationService locationService,
                                   UserRepository userRepository) {
        this.companionPostRepository = companionPostRepository;
        this.locationService = locationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PostResponse> list(
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<CompanionPurpose> purposes
    ) {
        return companionPostRepository.search(countryCode, cityCode, startDate, endDate, purposes)
                .stream()
                .map(post -> PostResponse.from(post, locationService))
                .toList();
    }

    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    public PostResponse detail(@PathVariable Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));
        post.getAuthor().getNickname();
        return PostResponse.from(post, locationService);
    }

    @PostMapping
    @Transactional
    public PostResponse create(@AuthenticationPrincipal CustomUserPrincipal principal,
                               @Valid @RequestBody PostCreateRequest request) {
        locationService.requireCity(request.cityCode());
         validateDateRange(request.startDate(), request.endDate());

        CompanionPost post = new CompanionPost(
                userRepository.getReferenceById(principal.getUserId()),
                request.cityCode(),
                request.startDate(),
                request.endDate(),
                request.timeSlot(),
                request.purposes(),
                request.maxParticipants(),
                request.genderPreference(),
                request.title(),
                request.content(),
                request.travelStyles()
        );

        return PostResponse.from(companionPostRepository.save(post), locationService);
    }

    @PatchMapping("/{postId}/close")
    @Transactional
    public PostResponse close(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));

        if (!post.getAuthor().getId().equals(principal.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the author can close this post.");
        }

        post.close();
        return PostResponse.from(post, locationService);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }
    }
}
