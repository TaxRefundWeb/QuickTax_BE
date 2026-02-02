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

        String token = resolveTokenFromCookie(request);

        // 💡 토큰이 있을 때만 검증 로직 수행 (try-catch 범위를 최소화)
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Long cpaId = jwtUtil.extractCpaId(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(cpaId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                // 토큰 만료 -> 응답 보내고 여기서 필터 종료 (return)
                sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
                return;
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                // 토큰 위조 -> 응답 보내고 여기서 필터 종료 (return)
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            } catch (Exception e) {
                // 기타 인증 에러 -> 응답 보내고 여기서 필터 종료 (return)
                sendErrorResponse(response, ErrorCode.AUTH403);
                return;
            }
        }

        // 💡 중요: 필터 체인 실행은 try-catch 바깥에서!
        // (토큰이 없거나 검증을 통과했으면 다음 단계로 진행)
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
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