package com.tripmate.application;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.chat.ChatService;
import com.tripmate.common.ApiException;
import com.tripmate.location.LocationService;
import com.tripmate.notification.NotificationService;
import com.tripmate.post.CompanionPost;
import com.tripmate.post.CompanionPostRepository;
import com.tripmate.post.PostStatus;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "동행 신청", description = "동행 모집 글에 신청하고, 내가 보낸 신청과 받은 신청을 관리하는 API")
@SecurityRequirement(name = "bearerAuth")
public class CompanionApplicationController {
    private final CompanionPostRepository companionPostRepository;
    private final CompanionApplicationRepository companionApplicationRepository;
    private final LocationService locationService;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final UserRepository userRepository;

    public CompanionApplicationController(CompanionPostRepository companionPostRepository,
                                          CompanionApplicationRepository companionApplicationRepository,
                                          LocationService locationService,
                                          NotificationService notificationService,
                                          ChatService chatService,
                                          UserRepository userRepository) {
        this.companionPostRepository = companionPostRepository;
        this.companionApplicationRepository = companionApplicationRepository;
        this.locationService = locationService;
        this.notificationService = notificationService;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}")
    @Transactional
    @Operation(summary = "동행 신청", description = "로그인한 사용자가 열린 동행 모집 글에 신청 메시지를 남기고 작성자에게 알림을 보냅니다.")
    public ApplicationResponse apply(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
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
        CompanionApplication savedApplication = companionApplicationRepository.save(application);
        notificationService.notifyApplicationReceived(savedApplication);
        return ApplicationResponse.from(savedApplication, locationService);
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내가 보낸 신청 목록 조회", description = "현재 로그인한 사용자가 다른 동행 모집 글에 보낸 신청 목록을 최신순으로 조회합니다.")
    public List<ApplicationResponse> myApplications(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User applicant = userRepository.getReferenceById(principal.getUserId());
        return companionApplicationRepository.findMyApplications(applicant)
                .stream()
                .map(application -> ApplicationResponse.from(application, locationService))
                .toList();
    }

    @GetMapping("/received")
    @Transactional(readOnly = true)
    @Operation(summary = "내가 받은 신청 목록 조회", description = "현재 로그인한 사용자가 작성한 동행 모집 글에 들어온 신청 목록을 최신순으로 조회합니다.")
    public List<ApplicationResponse> receivedApplications(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User author = userRepository.getReferenceById(principal.getUserId());
        return companionApplicationRepository.findReceivedApplications(author)
                .stream()
                .map(application -> ApplicationResponse.from(application, locationService))
                .toList();
    }

    @PatchMapping("/{applicationId}/accept")
    @Transactional
    @Operation(summary = "동행 신청 수락", description = "동행 모집 글 작성자가 받은 신청을 수락 상태로 변경하고 신청자에게 수락 알림을 보냅니다.")
    public ApplicationResponse accept(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long applicationId) {
        CompanionApplication application = getOwnedApplication(principal.getUserId(), applicationId);
        application.accept();
        chatService.openRoomForAcceptedApplication(application);
        notificationService.notifyApplicationAccepted(application);
        return ApplicationResponse.from(application, locationService);
    }

    @PatchMapping("/{applicationId}/reject")
    @Transactional
    @Operation(summary = "동행 신청 거절", description = "동행 모집 글 작성자가 받은 신청을 거절 상태로 변경하고 신청자에게 거절 알림을 보냅니다.")
    public ApplicationResponse reject(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long applicationId) {
        CompanionApplication application = getOwnedApplication(principal.getUserId(), applicationId);
        application.reject();
        notificationService.notifyApplicationRejected(application);
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
