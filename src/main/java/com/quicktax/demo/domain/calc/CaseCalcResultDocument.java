package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "case_calc_result_document")
@Getter
@NoArgsConstructor
public class CaseCalcResultDocument {

    @EmbeddedId
    private CaseCalcResultDocumentId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    @NotBlank
    private String url;

    public CaseCalcResultDocument(TaxCase taxCase, Integer caseYear, String url) {
        this.taxCase = taxCase;
        this.id = new CaseCalcResultDocumentId(taxCase.getCaseId(), caseYear);
        this.url = url;
    }
}
