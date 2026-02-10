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

    /**
     * 🚨 [핵심 추가] OPTIONS 요청(Preflight)일 경우, 필터 로직을 아예 실행하지 않고 통과시킵니다.
     * 이 설정이 없으면 내부 로직 어딘가에서 예외가 터지거나 막힐 위험이 있습니다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getMethod().equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더가 아닌 '쿠키'에서 토큰을 추출합니다.
        String token = resolveTokenFromCookie(request);

        // 2. 토큰이 있는 경우에만 검증 로직 수행
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Long cpaId = jwtUtil.extractCpaId(token);

                    // 인증 객체 생성 (권한은 비워둠 - 필요 시 jwtUtil에서 권한 추출하여 넣기)
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(cpaId, null, Collections.emptyList());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                log.warn("토큰 만료: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED); // 401
                return; // 🚨 여기서 리턴해서 필터 체인 끊기
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                log.warn("토큰 위조/손상: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID); // 403
                return;
            } catch (Exception e) {
                log.error("JWT 인증 중 알 수 없는 오류: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.AUTH403); // 403
                return;
            }
        }

        // 3. 토큰이 없거나(검증 로직 밖), 검증을 통과했으면 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

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

        // 수동 JSON 생성 (간단한 형태라 문제 없음)
        String json = String.format(
                "{\"isSuccess\":false, \"code\":\"%s\", \"message\":\"%s\", \"result\":null}",
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(json);
    }
}