package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {
    private String name;
    private String rrn;
    private String phone; // 추가된 필드
    private String address;
    private String bank;

    @JsonProperty("bank_number")
    private String bankNumber;

    @JsonProperty("nationality_code")
    private String nationalityCode;

    @JsonProperty("nationality_name")
    private String nationalityName;

    @JsonProperty("final_fee_percent")
    private String finalFeePercent; // String 타입으로 변경
}