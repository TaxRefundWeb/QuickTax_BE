package com.quicktax.demo.config;

import com.quicktax.demo.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 쿠키에서 토큰 추출
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        try {
            // 2. 토큰 검증 및 인증 처리
            if (token != null && jwtUtil.validateToken(token)) {
                Long cpaId = jwtUtil.extractCpaId(token);

                // 인증 객체 생성 및 ContextHolder에 등록
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(cpaId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // 3. 정상적인 경우 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 💡 토큰 시간이 만료되었을 때 실행되는 블록
            sendErrorResponse(response, "AUTH401", "로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
        } catch (Exception e) {
            // 💡 그 외 잘못된 토큰 등 모든 인증 관련 예외 처리
            sendErrorResponse(response, "AUTH403", "인증 정보가 유효하지 않습니다.");
        }
    }

    /**
     * 필터 단계에서 발생한 에러를 JSON 응답으로 변환하여 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType("application/json;charset=UTF-8");

        // 약속한 공통 응답 포맷 (isSuccess, code, message, result)
        String json = String.format(
                "{\"isSuccess\":false, \"code\":\"%s\", \"message\":\"%s\", \"result\":null}",
                code, message
        );

        response.getWriter().write(json);
    }
}