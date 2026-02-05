package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.LoginRequest;
import com.quicktax.demo.service.auth.AuthService;
import com.quicktax.demo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. 인증(Auth)", description = "회원가입, 로그인, 로그아웃 API")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @PostMapping("/login")
    @Operation(summary = "CPA 로그인", description = "아이디와 비밀번호를 검증하여 로그인합니다. 성공 시 JWT 토큰이 **HttpOnly 쿠키('accessToken')**에 담겨 반환됩니다.")
    public ApiResponse<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 로그인 로직 수행
        String token = authService.login(request.getCpaId(), request.getPassword());

        // 2. 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .domain(cookieDomain)
                .maxAge(Duration.ofHours(10))
                .build();

        // 3. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok("로그인 성공");
    }

    // 💡 [추가] 로그아웃 API
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "accessToken 쿠키의 유효시간을 0으로 설정하여 삭제 처리합니다.")
    public ApiResponse<String> logout(HttpServletResponse response) {

        // 쿠키 삭제를 위해 로그인 때와 '동일한 조건'으로 Max-Age만 0으로 설정
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .domain(cookieDomain) // 도메인이 다르면 삭제되지 않으니 주의!
                .maxAge(0) // 💡 즉시 만료 (삭제)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok("로그아웃 성공");
    }
}