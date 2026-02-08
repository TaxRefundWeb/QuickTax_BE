package com.quicktax.demo.domain.refund;

import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.domain.calc.CaseCalcResult; // 💡 Import 추가
import com.quicktax.demo.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpa_id")
    private TaxCompany taxCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // --- (기존 필드들 유지) ---

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

    // --- 💡 [추가] 양방향 연결 설정 ---
    // RefundCase 하나에 여러 개의 계산 결과(CaseCalcResult)가 달림
    // mappedBy = "refundCase": CaseCalcResult 클래스의 'refundCase' 필드가 주인이라는 뜻
    @OneToMany(mappedBy = "refundCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 패턴 사용 시 리스트 초기화 유지
    private List<CaseCalcResult> results = new ArrayList<>();
}