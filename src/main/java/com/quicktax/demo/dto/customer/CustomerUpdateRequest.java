package com.quicktax.demo.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    private String name;
    private String rrn;
    private String phone; // 💡 이 필드가 있어야 request.getPhone()이 작동합니다.
    private String address;
    private String bank;

    @JsonProperty("bank_number")
    private String bankNumber;

    @JsonProperty("nationality_code")
    private String nationalityCode;

    @JsonProperty("nationality_name")
    private String nationalityName;

    @JsonProperty("final_fee_percent")
    private String finalFeePercent;
}