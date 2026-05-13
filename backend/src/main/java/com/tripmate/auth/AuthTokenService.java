package com.tripmate.auth;

import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class AuthTokenService {
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthTokenService(JwtTokenProvider jwtTokenProvider, StringRedisTemplate stringRedisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String issueToken(Long userId) {
        String token = jwtTokenProvider.createToken(userId);
        Claims claims = jwtTokenProvider.parseClaims(token);
        long ttlMillis = claims.getExpiration().getTime() - new Date().getTime();

        if (ttlMillis > 0) {
            stringRedisTemplate.opsForValue()
                    .set(tokenKey(claims.getId()), String.valueOf(userId), Duration.ofMillis(ttlMillis));
        }

        return token;
    }

    public boolean isTokenActive(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenKey(jwtTokenProvider.getTokenId(token))));
    }

    public void revokeToken(String token) {
        stringRedisTemplate.delete(tokenKey(jwtTokenProvider.getTokenId(token)));
    }

    private String tokenKey(String tokenId) {
        return TOKEN_KEY_PREFIX + tokenId;
    }
}
