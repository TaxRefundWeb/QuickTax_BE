package com.quicktax.demo.domain.refund;

import com.quicktax.demo.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefundCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long caseId;

    private LocalDate caseDate;      // 사례 날짜 (예: 2026-01-28)
    private String scenarioCode;     // 시나리오명 (예: 청년 감면)
    private Long determinedTaxAmount; // 결정세액
    private Long refundAmount;        // 환급액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;       // 어떤 고객의 기록인지 연결
}