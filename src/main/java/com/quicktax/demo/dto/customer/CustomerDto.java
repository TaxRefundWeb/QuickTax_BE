package com.quicktax.demo.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1. 개별 고객 정보를 담는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long customerid;
    private String name;
    private String birthdate;
    private String rrn;
}

