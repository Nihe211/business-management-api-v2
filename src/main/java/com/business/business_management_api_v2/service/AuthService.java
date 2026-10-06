package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.request.LoginRequest;
import com.business.business_management_api_v2.dto.request.RegisterRequest;
import com.business.business_management_api_v2.dto.response.AuthResponse;
import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.entity.RefreshToken;
import com.business.business_management_api_v2.entity.User;
import com.business.business_management_api_v2.enums.Role;
import com.business.business_management_api_v2.exception.ConflictException;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.exception.UnauthorizedException;
import com.business.business_management_api_v2.mapper.UserMapper;
import com.business.business_management_api_v2.repository.RefreshTokenRepo;
import com.business.business_management_api_v2.repository.UserRepo;
import com.business.business_management_api_v2.security.CustomUserDetails;
import com.business.business_management_api_v2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepo refreshTokenRepo;
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;
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

    public AuthTokens login(LoginRequest request){
        String username = request.getUsername().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,request.getPassword()));
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        User user = userRepo.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user có ID: " + principal.getId()));
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthTokens refresh(String rawToken) {
        // TODO 1: rawToken null hoặc blank -> ném UnauthorizedException("Không tìm thấy refresh token")
        if (rawToken == null||rawToken.isBlank()){
            throw new UnauthorizedException("Không tìm thấy refresh token");
        }
        // TODO 2: tìm RefreshToken stored bằng refreshTokenRepo.findByTokenHash(hash(rawToken))
        //         không có -> UnauthorizedException("Refresh token không hợp lệ")
        RefreshToken stored = refreshTokenRepo.findByTokenHash(hash(rawToken)).
                orElseThrow(()-> new UnauthorizedException("Refresh token không hợp lệ"));
        // TODO 3: nếu stored.isRevoked():
        //           gọi refreshTokenRepo.revokeAllByUserId(stored.getUser().getId())
        //           rồi ném UnauthorizedException("Refresh token đã bị thu hồi, vui lòng đăng nhập lại")
        if (stored.isRevoked()){
            refreshTokenRepo.revokeAllByUserId(stored.getUser().getId());
            throw new UnauthorizedException("Refresh token đã bị thu hồi, vui lòng đăng nhập lại");
        }
        // TODO 4: nếu stored.getExpiresAt().isBefore(Instant.now()) -> UnauthorizedException("Refresh token đã hết hạn")
        if (stored.getExpiresAt().isBefore(Instant.now())){
            throw new UnauthorizedException("Refresh token đã hết hạn");
        }
        // TODO 5: nếu !stored.getUser().isActive() -> UnauthorizedException("Tài khoản đã bị vô hiệu hoá")
        if (!stored.getUser().isActive()){
            throw new UnauthorizedException("Tài khoản đã bị vô hiệu hoá");
        }
        // TODO 6: stored.setRevoked(true);     // dùng xong là bỏ (rotation)
        stored.setRevoked(true);
        // TODO 7: return issueTokens(stored.getUser());
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(String rawToken) {
        // TODO: rawToken null hoặc blank thì return
        //       tìm theo hash(rawToken); nếu có thì setRevoked(true)  (dùng .ifPresent)
        //       không có cũng KHÔNG báo lỗi: đăng xuất lặp lại vẫn coi là thành công
        if (rawToken==null||rawToken.isBlank()){
            return;
        }
        refreshTokenRepo.findByTokenHash(hash(rawToken))
                .ifPresent(stored -> stored.setRevoked(true));

    }

    private AuthTokens issueTokens(User user){
        CustomUserDetails principal = new CustomUserDetails(user);

        String rawRefreshToken = generateRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawRefreshToken));
        entity.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepo.save(entity);

        AuthResponse body = AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
        return new AuthTokens(body,rawRefreshToken);
    }

    private String generateRawToken(){
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        }catch (NoSuchAlgorithmException ex){
            throw new IllegalStateException(ex);
        }
    }
}
