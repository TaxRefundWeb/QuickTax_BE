package com.quicktax.demo.domain.ocr;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ocr_result_per_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrResultPerCompany {

    @EmbeddedId
    private OcrResultPerCompanyId id;

    @MapsId("ocrResultId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "case_id", referencedColumnName = "case_id"),
            @JoinColumn(name = "case_year", referencedColumnName = "case_year")
    })
    private OcrResult ocrResult;

    @Column(name = "salary")
    private Long salary;

    public OcrResultPerCompany(OcrResult ocrResult, Integer companyId, Long salary) {
        this.ocrResult = ocrResult;
        this.id = new OcrResultPerCompanyId(ocrResult.getId(), companyId);
        this.salary = salary;
    }
}

