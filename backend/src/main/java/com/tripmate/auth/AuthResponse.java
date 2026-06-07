package com.tripmate.auth;

public record AuthResponse(String token, AuthUserResponse user) {}
