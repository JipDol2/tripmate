package com.tripmate.report;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "신고", description = "사용자나 게시글에 대한 신고를 접수하는 API")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportController(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "신고 접수", description = "현재 로그인한 사용자가 특정 사용자 또는 동행 모집 글을 사유와 상세 내용과 함께 신고합니다.")
    public Map<String, String> report(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                      @Valid @RequestBody ReportCreateRequest request) {
        reportRepository.save(new Report(
                userRepository.getReferenceById(principal.getUserId()),
                request.targetUserId(),
                request.targetPostId(),
                request.reason(),
                request.detail()
        ));
        return Map.of("message", "Reported.");
    }
}
