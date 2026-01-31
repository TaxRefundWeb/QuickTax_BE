package com.quicktax.demo.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * STG에서 Swagger "Try it out" 호출이 http로 나가서(=HTTPS 페이지에서 Mixed Content) 브라우저가 막히는 문제 해결.
 *
 * 해결:
 * - OpenAPI servers를 상대경로("/")로 강제해서 Swagger UI가 현재 origin(https://api.quicktax.site)을 사용하게 만든다.
 */
@Configuration
@Profile("stg")
public class SwaggerUiServerFixConfig {

    @Bean
    public OpenApiCustomizer forceRelativeServerUrl() {
        return openApi -> openApi.setServers(List.of(new Server().url("/")));
    }
}
