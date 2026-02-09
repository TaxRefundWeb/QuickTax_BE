// dto/refund/RefundYearCase.java
package com.quicktax.demo.dto.refund;

import java.util.List;

public record RefundYearCase(
        int case_year,
        boolean spouse_yn,
        boolean child_yn,
        boolean reduction_yn,
        List<RefundCompany> companies,
        RefundSpouse spouse,
        List<RefundChild> children
) {}
