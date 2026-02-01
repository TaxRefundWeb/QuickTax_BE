package com.quicktax.demo.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 1. 공통 에러
    BADREQ400(HttpStatus.BAD_REQUEST, "BADREQ400", "잘못된 요청입니다."),
    COMMON404(HttpStatus.NOT_FOUND, "COMMON404", "대상을 찾을 수 없습니다."),
    COMMON429(HttpStatus.TOO_MANY_REQUESTS, "COMMON429", "요청이 너무 많습니다."),
    COMMON500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류입니다."),

    // 2. 인증/권한 일반 에러
    AUTH401(HttpStatus.UNAUTHORIZED, "AUTH401", "로그인이 필요합니다."),
    AUTH403(HttpStatus.FORBIDDEN, "AUTH403", "권한이 없습니다."),

    // 💡 3. JWT 토큰 관련 에러 (세분화)
    // - 만료됨: 401 (재로그인 유도)
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401", "로그인 세션이 만료되었습니다. 다시 로그인해주세요."),

    // - 위조/손상됨: 403 (보안 위협 또는 잘못된 접근 차단)
    TOKEN_INVALID(HttpStatus.FORBIDDEN, "AUTH403", "유효하지 않은 인증 토큰입니다."),
    TOKEN_MALFORMED(HttpStatus.FORBIDDEN, "AUTH403", "잘못된 형식의 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.FORBIDDEN, "AUTH403", "지원되지 않는 토큰 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }

    public ApiException exception() {
        return new ApiException(this);
    }

    public ApiException exception(String message) {
        return new ApiException(this, message);
    }
}