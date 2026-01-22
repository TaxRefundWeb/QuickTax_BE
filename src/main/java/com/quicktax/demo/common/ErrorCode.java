package com.quicktax.demo.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BADREQ400(HttpStatus.BAD_REQUEST, "BADREQ400", "잘못된 요청입니다."),
    AUTH401(HttpStatus.UNAUTHORIZED, "AUTH401", "로그인이 필요합니다."),
    AUTH403(HttpStatus.FORBIDDEN, "AUTH403", "권한이 없습니다."),
    COMMON404(HttpStatus.NOT_FOUND, "COMMON404", "대상을 찾을 수 없습니다."),
    COMMON429(HttpStatus.TOO_MANY_REQUESTS, "COMMON429", "요청이 너무 많습니다."),
    COMMON500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류입니다.");

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
