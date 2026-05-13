package com.tripmate.application;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.common.ApiException;
import com.tripmate.location.LocationService;
import com.tripmate.post.CompanionPost;
import com.tripmate.post.CompanionPostRepository;
import com.tripmate.post.PostStatus;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class CompanionApplicationController {
    private final CompanionPostRepository companionPostRepository;
    private final CompanionApplicationRepository companionApplicationRepository;
    private final LocationService locationService;
    private final UserRepository userRepository;

    public CompanionApplicationController(CompanionPostRepository companionPostRepository,
                                          CompanionApplicationRepository companionApplicationRepository,
                                          LocationService locationService,
                                          UserRepository userRepository) {
        this.companionPostRepository = companionPostRepository;
        this.companionApplicationRepository = companionApplicationRepository;
        this.locationService = locationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}")
    @Transactional
    public ApplicationResponse apply(@AuthenticationPrincipal CustomUserPrincipal principal,
                                     @PathVariable Long postId,
                                     @Valid @RequestBody ApplicationCreateRequest request) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));

        if (post.getAuthor().getId().equals(principal.getUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot apply to your own post.");
        }

        if (post.getStatus() != PostStatus.OPEN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This post is already closed.");
        }

        User applicant = userRepository.getReferenceById(principal.getUserId());
        if (companionApplicationRepository.existsByPostAndApplicant(post, applicant)) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already applied to this post.");
        }

        CompanionApplication application = new CompanionApplication(post, applicant, request.message());
        return ApplicationResponse.from(companionApplicationRepository.save(application), locationService);
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications(@AuthenticationPrincipal CustomUserPrincipal principal) {
        User applicant = userRepository.getReferenceById(principal.getUserId());
        return companionApplicationRepository.findMyApplications(applicant)
                .stream()
                .map(application -> ApplicationResponse.from(application, locationService))
                .toList();
    }

    @GetMapping("/received")
    @Transactional(readOnly = true)
    public List<ApplicationResponse> receivedApplications(@AuthenticationPrincipal CustomUserPrincipal principal) {
        User author = userRepository.getReferenceById(principal.getUserId());
        return companionApplicationRepository.findReceivedApplications(author)
                .stream()
                .map(application -> ApplicationResponse.from(application, locationService))
                .toList();
    }

    @PatchMapping("/{applicationId}/accept")
    @Transactional
    public ApplicationResponse accept(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long applicationId) {
        CompanionApplication application = getOwnedApplication(principal.getUserId(), applicationId);
        application.accept();
        return ApplicationResponse.from(application, locationService);
    }

    @PatchMapping("/{applicationId}/reject")
    @Transactional
    public ApplicationResponse reject(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long applicationId) {
        CompanionApplication application = getOwnedApplication(principal.getUserId(), applicationId);
        application.reject();
        return ApplicationResponse.from(application, locationService);
    }

    private CompanionApplication getOwnedApplication(Long userId, Long applicationId) {
        CompanionApplication application = companionApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found."));

        if (!application.getPost().getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the post author can process applications.");
        }

        return application;
    }
}
