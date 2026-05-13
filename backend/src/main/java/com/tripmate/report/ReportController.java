package com.tripmate.report;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.user.UserRepository;
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
public class ReportController {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportController(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Transactional
    public Map<String, String> report(@AuthenticationPrincipal CustomUserPrincipal principal,
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
