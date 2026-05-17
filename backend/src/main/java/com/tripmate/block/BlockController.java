package com.tripmate.block;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.common.ApiException;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blocks")
@Tag(name = "차단", description = "사용자를 차단해 원하지 않는 상호작용을 제한하는 API")
@SecurityRequirement(name = "bearerAuth")
public class BlockController {
    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    public BlockController(UserBlockRepository userBlockRepository, UserRepository userRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{targetUserId}")
    @Transactional
    @Operation(summary = "사용자 차단", description = "현재 로그인한 사용자가 지정한 사용자를 차단 목록에 추가합니다. 이미 차단한 사용자는 중복 저장하지 않습니다.")
    public Map<String, String> block(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                     @PathVariable Long targetUserId) {
        if (principal.getUserId().equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot block yourself.");
        }

        User blocker = userRepository.getReferenceById(principal.getUserId());
        if (!userBlockRepository.existsByBlockerAndBlockedUserId(blocker, targetUserId)) {
            userBlockRepository.save(new UserBlock(blocker, targetUserId));
        }

        return Map.of("message", "Blocked.");
    }
}
