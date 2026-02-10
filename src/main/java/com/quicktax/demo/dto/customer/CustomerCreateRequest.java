package com.quicktax.demo.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerCreateRequest {
    private String name;
    private String rrn;
    private String phone;     // 추가
    private String address;   // 추가
    private String bank;

    @JsonProperty("bank_number")
    private String bankNumber;

    @JsonProperty("nationality_code")
    private String nationalityCode;

    @JsonProperty("nationality_name")
    private String nationalityName;

    @JsonProperty("final_fee_percent")
    private String finalFeePercent; // 예시의 "33"을 위해 String으로 설정
}