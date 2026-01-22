package com.quicktax.demo.domain.ocr;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class OcrResultPerCompanyId implements Serializable {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "caseId", column = @Column(name = "case_id")),
            @AttributeOverride(name = "caseYear", column = @Column(name = "case_year"))
    })
    private OcrResultId ocrResultId;

    @Column(name = "company_id")
    private Integer companyId;

    public OcrResultPerCompanyId(OcrResultId ocrResultId, Integer companyId) {
        this.ocrResultId = ocrResultId;
        this.companyId = companyId;
    }
}
