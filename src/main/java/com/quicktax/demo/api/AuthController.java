package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.LoginRequest;
import com.quicktax.demo.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        String token = authService.login(request.getCpaId(), request.getPassword());

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 테스트용
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 10); // 10시간
        response.addCookie(cookie);

        return ApiResponse.ok("로그인 성공 및 쿠키 발급 완료");
    }
}