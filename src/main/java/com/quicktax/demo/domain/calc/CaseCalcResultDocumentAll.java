package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "case_calc_result_document_all")
@Getter
@NoArgsConstructor
public class CaseCalcResultDocumentAll {

    @Id
    @Column(name = "case_id")
    private Long caseId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    // total_URL TEXT
    @Column(name = "total_url", nullable = false, columnDefinition = "text")
    @NotNull
    private String totalUrl;

    // total_refund_amount BIGINT
    @Column(name = "total_refund_amount", nullable = false)
    @NotNull
    private Long totalRefundAmount;

    public CaseCalcResultDocumentAll(TaxCase taxCase, String totalUrl, Long totalRefundAmount) {
        this.taxCase = taxCase;
        this.caseId = taxCase.getCaseId();
        this.totalUrl = totalUrl;
        this.totalRefundAmount = totalRefundAmount;
    }
}
