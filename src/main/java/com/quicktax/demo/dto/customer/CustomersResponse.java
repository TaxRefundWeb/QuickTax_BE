package com.quicktax.demo.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List; /**
 * 2. result 안에 "customers": [] 구조를 만들기 위한 래퍼 클래스
 */
@Getter
@AllArgsConstructor
public class CustomersResponse {
    private List<CustomerDto> customers;
}
