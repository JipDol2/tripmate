package com.tripmate.post;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.application.ApplicationStatus;
import com.tripmate.application.CompanionApplicationRepository;
import com.tripmate.common.ApiException;
import com.tripmate.location.LocationService;
import com.tripmate.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "동행 모집 글", description = "여행 동행 모집 글을 검색, 조회, 생성, 마감하는 API")
public class CompanionPostController {
    private final CompanionPostRepository companionPostRepository;
    private final CompanionApplicationRepository companionApplicationRepository;
    private final LocationService locationService;
    private final UserRepository userRepository;

    public CompanionPostController(CompanionPostRepository companionPostRepository,
                                   CompanionApplicationRepository companionApplicationRepository,
                                   LocationService locationService,
                                   UserRepository userRepository) {
        this.companionPostRepository = companionPostRepository;
        this.companionApplicationRepository = companionApplicationRepository;
        this.locationService = locationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "동행 모집 글 목록 조회", description = "국가, 도시, 여행 기간, 시간대, 목적, 선호 나이대, 선호 성별 조건으로 동행 모집 글을 검색합니다.")
    public List<PostResponse> list(
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String timeSlot,
            @RequestParam(required = false) List<CompanionPurpose> purposes,
            @RequestParam(required = false) List<String> agePreferences,
            @RequestParam(required = false) String genderPreference
    ) {
        return companionPostRepository.search(countryCode, cityCode, startDate, endDate, timeSlot, purposes, agePreferences, genderPreference)
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    @Operation(summary = "동행 모집 글 상세 조회", description = "동행 모집 글 한 건의 상세 내용과 작성자, 여행지 정보를 조회합니다.")
    public PostResponse detail(@PathVariable Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));
        post.getAuthor().getNickname();
        return toPostResponse(post);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "동행 모집 글 작성", description = "로그인한 사용자가 여행지, 기간, 동행 목적, 모집 조건, 제목과 내용을 입력해 새 동행 모집 글을 작성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public PostResponse create(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
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
                request.agePreferences(),
                request.title(),
                request.content(),
                request.travelStyles()
        );

        return toPostResponse(companionPostRepository.save(post));
    }

    @PatchMapping("/{postId}/close")
    @Transactional
    @Operation(summary = "동행 모집 글 마감", description = "작성자 본인이 동행 모집 글의 상태를 마감으로 변경해 더 이상 신청을 받지 않도록 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public PostResponse close(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));

        if (!post.getAuthor().getId().equals(principal.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the author can close this post.");
        }

        post.close();
        return toPostResponse(post);
    }

    private PostResponse toPostResponse(CompanionPost post) {
        int currentParticipants = 1 + Math.toIntExact(companionApplicationRepository.countByPostAndStatus(post, ApplicationStatus.ACCEPTED));
        return PostResponse.from(post, locationService, currentParticipants);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }
    }
}
