// dto/refund/RefundClaimRequest.java
package com.quicktax.demo.dto.refund;

import java.util.List;

public record RefundClaimRequest(
        List<RefundYearCase> cases
) {}
