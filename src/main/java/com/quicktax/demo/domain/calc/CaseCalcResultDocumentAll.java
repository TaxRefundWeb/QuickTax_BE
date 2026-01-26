package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @Column(name = "url", nullable = false, columnDefinition = "text")
    @NotBlank
    private String url;

    public CaseCalcResultDocumentAll(TaxCase taxCase, String url) {
        this.taxCase = taxCase;
        this.caseId = taxCase.getCaseId();
        this.url = url;
    }
}
