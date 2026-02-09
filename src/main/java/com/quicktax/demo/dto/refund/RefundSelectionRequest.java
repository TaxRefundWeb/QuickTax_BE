// dto/refund/RefundSelectionRequest.java
package com.quicktax.demo.dto.refund;

import java.time.LocalDate;

public record RefundSelectionRequest(
        int claim_from,
        int claim_to,
        boolean reduction_yn,
        LocalDate reduction_start,
        LocalDate reduction_end,
        LocalDate claim_date
) {}
