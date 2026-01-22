package com.quicktax.demo.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiCommonResponsesConfig {

    @Bean
    public OpenApiCustomizer addCommonErrorResponses() {
        return (OpenAPI openApi) -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        ApiResponses responses = operation.getResponses();
                        if (responses == null) return;

                        // 이미 있으면 덮어쓰지 않음 (각 API가 따로 정의한 응답 존중)
                        putIfAbsent(responses, "400", "BADREQ400", "잘못된 요청입니다.");
                        putIfAbsent(responses, "401", "AUTH401", "로그인이 필요합니다.");
                        putIfAbsent(responses, "403", "AUTH403", "권한이 없습니다.");
                        putIfAbsent(responses, "404", "COMMON404", "대상을 찾을 수 없습니다.");
                        putIfAbsent(responses, "429", "COMMON429", "요청이 너무 많습니다.");
                        putIfAbsent(responses, "500", "COMMON500", "서버 오류입니다.");
                    })
            );
        };
    }

    private void putIfAbsent(ApiResponses responses, String httpStatus, String code, String message) {
        if (responses.containsKey(httpStatus)) return;

        // 실패 포맷 스키마
        Schema<?> schema = new ObjectSchema()
                .addProperty("isSuccess", new BooleanSchema().example(false))
                .addProperty("code", new StringSchema().example(code))
                .addProperty("message", new StringSchema().example(message))
                .addProperty("result", new ObjectSchema().example(new java.util.HashMap<>()));

        MediaType mediaType = new MediaType().schema(schema);

        io.swagger.v3.oas.models.responses.ApiResponse apiResponse =
                new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description(message)
                        .content(new Content().addMediaType("application/json", mediaType));

        responses.addApiResponse(httpStatus, apiResponse);
    }
}
