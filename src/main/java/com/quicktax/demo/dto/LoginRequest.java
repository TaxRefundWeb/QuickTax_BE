package com.quicktax.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// LoginRequest.java 예시
public class LoginRequest {
    private Long cpaId;
    private String password;
}