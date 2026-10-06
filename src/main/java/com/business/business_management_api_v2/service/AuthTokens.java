package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.response.AuthResponse;
import com.business.business_management_api_v2.entity.RefreshToken;

public record AuthTokens(AuthResponse response, String refreshToken) {
}
