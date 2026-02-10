package com.quicktax.demo.domain.ocr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OcrResultId implements Serializable {

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_year", nullable = false)
    private Integer caseYear;
}
