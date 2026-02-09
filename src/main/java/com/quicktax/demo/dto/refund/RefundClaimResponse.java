// dto/refund/RefundClaimResponse.java
package com.quicktax.demo.dto.refund;

import java.util.List;

public record RefundClaimResponse(
        List<Integer> saved_case_years
) {}
