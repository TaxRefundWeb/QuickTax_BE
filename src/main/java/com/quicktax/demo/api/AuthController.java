package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.LoginRequest;
import com.quicktax.demo.service.auth.AuthService;
import com.quicktax.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration; // 💡 Duration 클래스 import 필수

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // application.properties에서 설정을 가져옴 (로컬: localhost, 배포: .quicktax.site)
    @Value("${cookie.domain}")
    private String cookieDomain;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 로그인 로직 수행 (ID/PW 검증)
        String token = authService.login(request.getCpaId(), request.getPassword());

        // 2. 🍪 쿠키 생성 (요청하신 설정 적용)
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)              // 자바스크립트 접근 차단 (XSS 방지)
                .secure(true)                // HTTPS 전송 강제 (SameSite=None 필수)
                .sameSite("None")            // 서로 다른 도메인(프론트/백) 간 전송 허용
                .path("/")                   // 모든 경로에서 쿠키 유효
                .domain(cookieDomain)        // 환경에 맞는 도메인 설정 (.quicktax.site 등)
                .maxAge(Duration.ofHours(10)) // 💡 유효기간 10시간으로 설정
                .build();

        // 3. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok("로그인 성공");
    }
}