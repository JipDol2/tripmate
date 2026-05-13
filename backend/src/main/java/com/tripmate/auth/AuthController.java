package com.tripmate.auth;

import com.tripmate.common.ApiException;
import com.tripmate.user.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthTokenService authTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/register")
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.ageRange(),
                request.gender() == null ? Gender.PRIVATE : request.gender()
        );

        User savedUser = userRepository.save(user);
        String token = authTokenService.issueToken(savedUser.getId());

        return new AuthResponse(token, UserResponse.from(savedUser));
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        String token = authTokenService.issueToken(user.getId());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = extractBearerToken(authorization);

        if (token == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        try {
            authTokenService.revokeToken(token);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return ResponseEntity.noContent().build();
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }
}
