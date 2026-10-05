package com.business.business_management_api_v2.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;        // tính bằng giây
    private UserResponse user;
}
