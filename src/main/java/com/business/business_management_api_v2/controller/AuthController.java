package com.business.business_management_api_v2.controller;

import com.business.business_management_api_v2.dto.request.LoginRequest;
import com.business.business_management_api_v2.dto.request.RegisterRequest;
import com.business.business_management_api_v2.dto.response.AuthResponse;
import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.service.AuthService;
import com.business.business_management_api_v2.service.AuthTokens;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";
    private final AuthService authService;
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return withRefreshCookie(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE,required = false) String refreshToken){
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE,required = false) String refreshToken){
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,buildCookie("",Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie buildCookie(String value, Duration maxAge){
        return ResponseCookie.from(REFRESH_COOKIE,value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthTokens tokens){
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,buildCookie(tokens.refreshToken(),Duration.ofMillis(refreshExpirationMs)).toString())
                .body(tokens.response());
    }
}
