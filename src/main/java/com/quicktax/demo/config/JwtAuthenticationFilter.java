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
            // 💡 만료된 경우: ErrorCode.TOKEN_EXPIRED (HTTP 401)
            sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
            // 💡 위조/손상된 경우: ErrorCode.TOKEN_INVALID (HTTP 403)
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            // 💡 토큰이 비어있거나 잘못된 경우
            sendErrorResponse(response, ErrorCode.BADREQ400);
        } catch (Exception e) {
            // 💡 그 외 알 수 없는 오류
            sendErrorResponse(response, ErrorCode.AUTH403);
        }
    }

    /**
     * ✅ 수정된 에러 응답 메서드
     * - ErrorCode Enum 하나만 받아서 Status와 Body를 모두 세팅합니다.
     * - 더 이상 하드코딩된 401을 보내지 않습니다.
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        // 1. Enum에 정의된 HTTP Status(401, 403 등)를 그대로 설정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        // 2. Enum에 정의된 코드(AUTH401..)와 메시지 사용
        String json = String.format(
                "{\"isSuccess\":false, \"code\":\"%s\", \"message\":\"%s\", \"result\":null}",
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(json);
    }
}