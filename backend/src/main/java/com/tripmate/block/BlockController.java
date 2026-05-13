package com.tripmate.block;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.common.ApiException;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
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
public class BlockController {
    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    public BlockController(UserBlockRepository userBlockRepository, UserRepository userRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{targetUserId}")
    @Transactional
    public Map<String, String> block(@AuthenticationPrincipal CustomUserPrincipal principal,
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
