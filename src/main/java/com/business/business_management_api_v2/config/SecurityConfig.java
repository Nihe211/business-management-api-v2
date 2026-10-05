package com.business.business_management_api_v2.config;

import com.business.business_management_api_v2.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf->csrf.disable())
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/error","/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e->e
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(request, response, 401, "Unauthorized", "Chưa đăng nhập hoặc token không hợp lệ"))
                        .accessDeniedHandler((req, res, ex)->
                                writeError(req, res, 403, "Forbidden", "Bạn không có quyền truy cập")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static void writeError(HttpServletRequest req, HttpServletResponse res,
                                   int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + message + "\","
                + "\"path\":\"" + req.getRequestURI() + "\"}");
    }
}
