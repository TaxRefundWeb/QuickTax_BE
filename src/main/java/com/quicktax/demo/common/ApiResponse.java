package com.quicktax.demo.common;

import java.util.Map;

public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        String message,
        T result
) {
    // 기본값
    public static <T> ApiResponse<T> ok(T result) {
        return new ApiResponse<>(true, "COMMON200", "성공입니다.", result);
    }
    // 커스텀용
    public static <T> ApiResponse<T> ok(String code, String message, T result) {
        return new ApiResponse<>(true, code, message, result);
    }

    // 실패일 때 result = {} 가 되게끔 했음
    public static ApiResponse<Map<String, Object>> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, Map.of());
    }
}
