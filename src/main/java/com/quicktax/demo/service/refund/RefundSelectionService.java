package com.quicktax.demo.service.refund;

import com.quicktax.demo.dto.refund.*;

public interface RefundSelectionService {

    Long createCase(
            Long cpaId,
            Long customerId,
            RefundSelectionRequest request
    );

    RefundClaimResponse saveRefundClaims(
            Long cpaId,
            Long caseId,
            RefundClaimRequest request
    );
}
