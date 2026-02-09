package com.quicktax.demo.dto.past;

import java.time.LocalDate;
import java.util.List;

public record PastHistoryItem(
        Long case_id,
        LocalDate claim_date,
        String scenario_code,
        String scenario_text,
        Long determined_tax_amount,
        Long refund_amount,
        List<PastYearUrl> urls
) {}
