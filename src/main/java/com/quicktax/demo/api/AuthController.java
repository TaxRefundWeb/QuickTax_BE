package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.LoginRequest;
import com.quicktax.demo.service.auth.AuthService;
import com.quicktax.demo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<String> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String token = authService.login(request.getCpaId(), request.getPassword());

        ResponseCookie cookie = buildAccessTokenCookie(httpRequest, token, Duration.ofHours(10));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok("로그인 성공");
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "accessToken 쿠키의 유효시간을 0으로 설정하여 삭제 처리합니다.")
    public ApiResponse<String> logout(HttpServletRequest httpRequest, HttpServletResponse response) {

        // 삭제도 로그인과 "동일한 옵션"으로 맞춰야 함
        ResponseCookie cookie = buildAccessTokenCookie(httpRequest, "", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok("로그아웃 성공");
    }

    /**
     * ✅ 핵심:
     * - localhost / http 환경에서는 Secure/None/Domain 때문에 쿠키가 저장/전송이 안 됨
     * - 그래서 환경에 따라 쿠키 옵션을 바꿔서 "실제로 붙는 쿠키"를 만든다.
     */
    private ResponseCookie buildAccessTokenCookie(HttpServletRequest req, String token, Duration maxAge) {
        String host = req.getServerName();
        boolean isLocalHost = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
        boolean isHttps = req.isSecure(); // https면 true (리버스 프록시면 X-Forwarded-Proto 세팅 필요)

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge);

        if (isLocalHost || !isHttps) {
            // ✅ 로컬/HTTP: Secure 쿠키가 안 붙으니까 false + SameSite=Lax (None은 Secure 없으면 브라우저가 거부)
            return builder
                    .secure(false)
                    .sameSite("Lax")
                    .build();
        }

        // ✅ 운영/스테이징 HTTPS: 서브도메인 공유를 위해 Domain=.quicktax.site + SameSite=None + Secure
        return builder
                .secure(true)
                .sameSite("None")
                .domain(cookieDomain)
                .build();
    }
}
