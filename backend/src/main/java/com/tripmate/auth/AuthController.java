package com.tripmate.auth;

import com.tripmate.common.ApiException;
import com.tripmate.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 등 인증 토큰을 관리하는 API")
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
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 기본 프로필 정보를 받아 새 사용자를 생성하고 JWT 토큰과 사용자 정보를 반환합니다.")
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

        return new AuthResponse(token, AuthUserResponse.from(savedUser));
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증한 뒤 인증에 사용할 JWT 토큰과 사용자 정보를 반환합니다.")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        String token = authTokenService.issueToken(user.getId());
        AuthResponse authResponse = new AuthResponse(token, AuthUserResponse.from(user));
        return authResponse;
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Authorization 헤더의 Bearer 토큰을 무효화해 이후 요청에서 사용할 수 없도록 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
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
