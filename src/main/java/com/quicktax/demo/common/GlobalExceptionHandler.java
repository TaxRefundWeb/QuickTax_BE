package com.quicktax.demo.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 커스텀 예외 -> 네가 정한 code/status 그대로
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleApi(ApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // 2) @Valid 검증 실패 -> 400 BADREQ400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValid(MethodArgumentNotValidException e) {
        // 너 정책: errors 안 넣음. message는 첫 에러 메시지로만 (없으면 기본)
        String msg = ErrorCode.BADREQ400.getMessage();
        var first = e.getBindingResult().getFieldErrors().stream().findFirst();
        if (first.isPresent() && first.get().getDefaultMessage() != null) {
            msg = first.get().getDefaultMessage();
        }
        return ResponseEntity.status(ErrorCode.BADREQ400.getStatus())
                .body(ApiResponse.fail(ErrorCode.BADREQ400.getCode(), msg));
    }

    // 3) 파라미터 타입 오류/누락/JSON 파싱 실패 -> 400 BADREQ400
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadReq(Exception e) {
        return ResponseEntity.status(ErrorCode.BADREQ400.getStatus())
                .body(ApiResponse.fail(ErrorCode.BADREQ400.getCode(), ErrorCode.BADREQ400.getMessage()));
    }

    // 4) 그 외 -> 500 COMMON500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnknown(Exception e) {
        return ResponseEntity.status(ErrorCode.COMMON500.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON500.getCode(), ErrorCode.COMMON500.getMessage()));
    }

    // 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle404(NoHandlerFoundException e) {
        return ResponseEntity.status(ErrorCode.COMMON404.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON404.getCode(), ErrorCode.COMMON404.getMessage()));
    }

}
