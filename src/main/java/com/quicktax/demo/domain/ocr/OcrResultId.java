package com.quicktax.demo.domain.ocr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class OcrResultId implements Serializable {

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "case_year")
    private Integer caseYear;

    public OcrResultId(Long caseId, Integer caseYear) {
        this.caseId = caseId;
        this.caseYear = caseYear;
    }

}


