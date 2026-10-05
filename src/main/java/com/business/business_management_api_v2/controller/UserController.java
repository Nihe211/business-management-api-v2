package com.business.business_management_api_v2.controller;

import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.security.CustomUserDetails;
import com.business.business_management_api_v2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal){
        return userService.getById(principal.getId());
    }
}
