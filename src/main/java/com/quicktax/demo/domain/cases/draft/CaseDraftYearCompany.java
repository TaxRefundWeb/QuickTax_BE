package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "case_draft_year_company")
@Getter
@NoArgsConstructor
public class CaseDraftYearCompany {

    @EmbeddedId
    private CaseDraftYearCompanyId id;

    @MapsId("draftYearId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "case_id", referencedColumnName = "case_id", nullable = false),
            @JoinColumn(name = "case_year", referencedColumnName = "case_year", nullable = false)
    })
    private CaseDraftYear caseDraftYear;

    // ✅ 추가
    @Column(name = "small_business_yn", nullable = false)
    private boolean smallBusinessYn = false;

    @NotNull
    @Column(name = "case_work_start", nullable = false)
    private LocalDate caseWorkStart;

    @Column(name = "case_work_end")
    private LocalDate caseWorkEnd;

    @Size(max = 30)
    @Column(name = "business_number", length = 30)
    private String businessNumber;

    @AssertTrue(message = "case_work_start must be <= case_work_end")
    public boolean isWorkDateRangeValid() {
        return caseWorkEnd == null || !caseWorkStart.isAfter(caseWorkEnd);
    }

    // ✅ 생성자에 smallBusinessYn 파라미터 추가
    public CaseDraftYearCompany(
            CaseDraftYear caseDraftYear,
            Integer caseCompany,
            LocalDate caseWorkStart,
            LocalDate caseWorkEnd,
            String businessNumber,
            boolean smallBusinessYn
    ) {
        this.caseDraftYear = caseDraftYear;
        this.id = new CaseDraftYearCompanyId(caseDraftYear.getId(), caseCompany);
        this.smallBusinessYn = smallBusinessYn; // ✅ 추가
        this.caseWorkStart = caseWorkStart;
        this.caseWorkEnd = caseWorkEnd;
        this.businessNumber = businessNumber;
    }
}
