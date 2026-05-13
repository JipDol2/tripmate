package com.tripmate.auth;

import com.tripmate.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {}
