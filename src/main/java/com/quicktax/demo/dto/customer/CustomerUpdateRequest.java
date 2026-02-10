package com.quicktax.demo.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {

    @JsonProperty("address")
    private String address;

    @JsonProperty("bank")
    private String bank;

    @JsonProperty("bank_number")
    private String bankNumber;

    @JsonProperty("nationality_code")
    private String nationalityCode;

    @JsonProperty("nationality_name")
    private String nationalityName;

    @JsonProperty("final_fee_percent")
    private String finalFeePercent;

    @JsonProperty("phone")
    private String phone;
}
