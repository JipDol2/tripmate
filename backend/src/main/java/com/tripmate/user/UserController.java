package com.tripmate.user;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.chat.ChatRoomRepository;
import com.tripmate.chat.ChatRoomParticipantRepository;
import com.tripmate.common.ApiException;
import com.tripmate.location.LocationService;
import com.tripmate.post.CompanionPostRepository;
import com.tripmate.post.PostResponse;
import com.tripmate.review.CompanionReviewRepository;
import com.tripmate.review.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "내 프로필과 다른 사용자의 공개 프로필을 조회하거나 수정하는 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserRepository userRepository;
    private final CompanionPostRepository companionPostRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CompanionReviewRepository companionReviewRepository;
    private final LocationService locationService;

    public UserController(UserRepository userRepository,
                          CompanionPostRepository companionPostRepository,
                          ChatRoomParticipantRepository chatRoomParticipantRepository,
                          ChatRoomRepository chatRoomRepository,
                          CompanionReviewRepository companionReviewRepository,
                          LocationService locationService) {
        this.userRepository = userRepository;
        this.companionPostRepository = companionPostRepository;
        this.chatRoomParticipantRepository = chatRoomParticipantRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.companionReviewRepository = companionReviewRepository;
        this.locationService = locationService;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 계정 정보와 프로필 정보를 조회합니다.")
    public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User managedUser = userRepository.findById(principal.getUserId())
                .orElseThrow();
        return UserResponse.from(managedUser);
    }

    @GetMapping("/{userId}/profile")
    @Transactional(readOnly = true)
    @Operation(summary = "공개 프로필 조회", description = "지정한 사용자의 공개 프로필, 작성한 동행 모집 글, 받은 리뷰 목록을 조회합니다.")
    public PublicUserProfileResponse publicProfile(@PathVariable Long userId) {
        User profileUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        var posts = companionPostRepository.findByAuthorOrderByCreatedAtDesc(profileUser)
                .stream()
                .map((post) -> {
                    var endedRoom = chatRoomRepository.findFirstByPostIdAndEndedAtIsNotNullOrderByEndedAtDesc(post.getId()).orElse(null);
                    return PostResponse.from(
                            post,
                            locationService,
                            Math.max(1, Math.toIntExact(chatRoomParticipantRepository.countJoinedParticipantsByPost(post))),
                            endedRoom != null,
                            endedRoom == null ? null : endedRoom.getEndedAt()
                    );
                })
                .toList();
        var reviews = companionReviewRepository.findByRevieweeOrderByCreatedAtDesc(profileUser)
                .stream()
                .map(ReviewResponse::from)
                .toList();

        return PublicUserProfileResponse.from(profileUser, posts, reviews);
    }

    @PutMapping("/me")
    @Transactional
    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 닉네임, 나이대, 성별, 자기소개, 프로필 이미지, 여행 스타일, 언어 정보를 수정합니다.")
    public UserResponse updateMe(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                 @RequestBody ProfileUpdateRequest request) {
        User managedUser = userRepository.findById(principal.getUserId())
                .orElseThrow();

        managedUser.updateProfile(
                request.nickname(),
                request.ageRange(),
                request.gender() == null ? Gender.PRIVATE : request.gender(),
                request.bio(),
                request.profileImageUrl(),
                request.travelStyles(),
                request.languages()
        );

        return UserResponse.from(managedUser);
    }
}
