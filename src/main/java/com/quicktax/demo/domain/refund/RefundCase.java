package com.quicktax.demo.domain.refund;

import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "refund_case")
public class RefundCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;

    // 1단계: CPA(세무법인) 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpa_id")
    private TaxCompany taxCompany;

    // 2단계: 고객 정보 (나중에 입력)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // --- 1단계 입력 정보 ---

    // 💡 [수정] 다시 caseDate (LocalDate)로 복구 (getCaseDate() 에러 해결)
    @Column(name = "case_date")
    private LocalDate caseDate;

    @Column(name = "claim_start")
    private String claimStart;

    @Column(name = "claim_end")
    private String claimEnd;

    @Column(name = "reduction_yn", length = 10)
    private String reductionYn;

    @Column(name = "reduction_start")
    private String reductionStart;

    @Column(name = "reduction_end")
    private String reductionEnd;

    @Column(name = "status")
    private String status;

    // --- 결과 정보 ---

    @Column(name = "scenario_code")
    private String scenarioCode;

    @Column(name = "determined_tax_amount")
    private Long determinedTaxAmount;

    @Column(name = "refund_amount")
    private Long refundAmount;
}