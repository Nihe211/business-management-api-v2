package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.request.LoginRequest;
import com.business.business_management_api_v2.dto.request.RegisterRequest;
import com.business.business_management_api_v2.dto.response.AuthResponse;
import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.entity.User;
import com.business.business_management_api_v2.enums.Role;
import com.business.business_management_api_v2.exception.ConflictException;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.mapper.UserMapper;
import com.business.business_management_api_v2.repository.UserRepo;
import com.business.business_management_api_v2.security.CustomUserDetails;
import com.business.business_management_api_v2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(RegisterRequest request) {
        // TODO 1: chuẩn hoá email: trim() và toLowerCase()
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim().toLowerCase();
        // TODO 2: nếu username đã tồn tại -> ném ConflictException("Username đã tồn tại")
        if (userRepo.existsByUsername(username)) {
            throw new ConflictException("Username đã tồn tại");
        }
        // TODO 3: nếu email đã tồn tại   -> ném ConflictException("Email đã tồn tại")
        if (userRepo.existsByEmail(email)){
            throw new ConflictException("Email đã tồn tại");
        }
        // TODO 4: tạo User mới, gán username, email (đã chuẩn hoá), fullName, phone
        //         password = passwordEncoder.encode(request.getPassword())   <- KHÔNG gán mật khẩu thô
        //         role = Role.USER                                           <- luôn là USER, không lấy từ client
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        Role role = Role.USER;
        String password = passwordEncoder.encode(request.getPassword());

        user.setRole(role);
        user.setPassword(password);
        // TODO 5: save, rồi trả về userMapper.toResponse(...)
        User saved = userRepo.save(user);

        return userMapper.toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        // TODO 1: username = request.getUsername().trim().toLowerCase()
        String username = request.getUsername().trim().toLowerCase();
        // TODO 2: Authentication auth = authenticationManager.authenticate(
        //             new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        //         (sai mật khẩu thì dòng này tự ném BadCredentialsException)
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,request.getPassword()));
        // TODO 3: CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        CustomUserDetails pricipal = (CustomUserDetails) auth.getPrincipal();
        // TODO 4: String token = jwtService.generateAccessToken(principal);
        String token = jwtService.generateAccessToken(pricipal);
        // TODO 5: User user = userRepo.findById(principal.getId()) ... orElseThrow(ResourceNotFoundException)
        User user = userRepo.findById(pricipal.getId()).orElseThrow(()-> new ResourceNotFoundException("Không tìm thấy user có ID: " + pricipal.getId()));
        // TODO 6: return AuthResponse.builder()
        //             .accessToken(token)
        //             .tokenType("Bearer")
        //             .expiresIn(jwtService.getAccessExpirationMs() / 1000)
        //             .user(userMapper.toResponse(user))
        //             .build();
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationMs()/1000)
                .user(userMapper.toResponse(user))
                .build();
    }
}
