package com.tripmate.user;

import com.tripmate.auth.CustomUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public UserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        User managedUser = userRepository.findById(principal.getUserId())
                .orElseThrow();
        return UserResponse.from(managedUser);
    }

    @PutMapping("/me")
    @Transactional
    public UserResponse updateMe(@AuthenticationPrincipal CustomUserPrincipal principal,
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
