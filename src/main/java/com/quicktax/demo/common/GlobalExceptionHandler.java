package com.quicktax.demo.common;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleApi(ApiException e) {
        log.warn("ApiException: code={}, msg={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValid(MethodArgumentNotValidException e) {
        String msg = ErrorCode.BADREQ400.getMessage();
        var first = e.getBindingResult().getFieldErrors().stream().findFirst();
        if (first.isPresent() && first.get().getDefaultMessage() != null) {
            msg = first.get().getDefaultMessage();
        }
        log.warn("Validation failed: {}", msg);
        return ResponseEntity.status(ErrorCode.BADREQ400.getStatus())
                .body(ApiResponse.fail(ErrorCode.BADREQ400.getCode(), msg));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadReq(Exception e) {
        log.warn("Bad request: {}", e.toString());
        return ResponseEntity.status(ErrorCode.BADREQ400.getStatus())
                .body(ApiResponse.fail(ErrorCode.BADREQ400.getCode(), ErrorCode.BADREQ400.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle404(NoHandlerFoundException e) {
        return ResponseEntity.status(ErrorCode.COMMON404.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON404.getCode(), ErrorCode.COMMON404.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnknown(Exception e) {
        e.printStackTrace(); // ✅ 이거 하나로 콘솔에 이유가 찍힘
        return ResponseEntity.status(ErrorCode.COMMON500.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON500.getCode(), ErrorCode.COMMON500.getMessage()));
    }
}
