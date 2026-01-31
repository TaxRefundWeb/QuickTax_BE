package com.quicktax.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * prod 환경에서 Swagger 관련 경로를 "존재하지 않는 것처럼" 404로 응답시키는 필터.
 * - springdoc 자체는 prod에서 비활성화되어 있어야 함 (application-prod.properties)
 * - 그래도 안전장치로, 요청이 들어오면 404(JSON)로 즉시 종료
 */
@Component
@Profile("prod")
public class SwaggerBlockFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public SwaggerBlockFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isSwaggerPath(path)) {
            send404(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerPath(String path) {
        return path != null && (
                path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||
                        path.equals("/swagger-ui.html")
        );
    }

    private void send404(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorCode.COMMON404.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.fail(ErrorCode.COMMON404.getCode(), ErrorCode.COMMON404.getMessage())
                )
        );
    }
}
