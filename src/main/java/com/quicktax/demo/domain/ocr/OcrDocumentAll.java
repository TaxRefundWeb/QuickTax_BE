package com.quicktax.demo.domain.ocr;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ocr_document_all")
@Getter
@NoArgsConstructor
public class OcrDocumentAll {

    @Id
    @Column(name = "case_id")
    private Long caseId;

    // PK를 FK로 같이 쓰는 1:1
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    private String url;

    public OcrDocumentAll(TaxCase taxCase, String url) {
        this.taxCase = taxCase;
        this.caseId = taxCase.getCaseId();
        this.url = url;
    }
}
