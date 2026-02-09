// dto/refund/RefundCompany.java
package com.quicktax.demo.dto.refund;

import java.time.LocalDate;

public record RefundCompany(
        String business_number,
        LocalDate case_work_start,
        LocalDate case_work_end,
        boolean small_business_yn
) {}
