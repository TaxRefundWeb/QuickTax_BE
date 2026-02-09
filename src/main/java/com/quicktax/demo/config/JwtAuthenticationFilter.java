package com.quicktax.demo.config;

import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 💡 1. 헤더가 아닌 '쿠키'에서 토큰을 추출합니다.
        String token = resolveTokenFromCookie(request);

        // 2. 토큰이 있는 경우에만 검증 로직 수행
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Long cpaId = jwtUtil.extractCpaId(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(cpaId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                // 토큰 만료 -> 401 응답 후 필터 중단
                sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
                return;
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                // 토큰 위조/손상 -> 403 응답 후 필터 중단
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            } catch (Exception e) {
                // 기타 에러 -> 403 응답 후 필터 중단
                sendErrorResponse(response, ErrorCode.AUTH403);
                return;
            }
        }

        // 3. 토큰이 없거나 검증을 통과했으면 다음 필터로 진행
        // (Swagger나 비로그인 허용 경로는 여기서 통과됨)
        filterChain.doFilter(request, response);
    }

    /**
     * ✅ 핵심 수정: Authorization 헤더 대신 Cookie에서 accessToken을 찾음
     */
    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        String json = String.format(
                "{\"isSuccess\":false, \"code\":\"%s\", \"message\":\"%s\", \"result\":null}",
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(json);
    }
}