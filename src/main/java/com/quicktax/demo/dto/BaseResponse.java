package com.quicktax.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseResponse<T> {
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    // 성공 시 사용할 정적 생성자 메서드
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>(true, "COMMON200", "성공입니다.", result);
    }

    // 실패 시 사용할 정적 생성자 메서드
    public static <T> BaseResponse<T> onFailure(String code, String message, T result) {
        return new BaseResponse<>(false, code, message, result);
    }
}