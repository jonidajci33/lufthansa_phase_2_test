package com.planningpoker.identity.web.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {}
