package com.quicktax.demo.domain.cases;

import com.quicktax.demo.domain.customer.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "cases",
        indexes = {
                @Index(name = "idx_cases_customer_id", columnList = "customer_id")
        }
)
@Getter
@NoArgsConstructor
public class TaxCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "claim_from")
    private Integer claimFrom;

    @Column(name = "claim_to")
    private Integer claimTo;

    @NotNull
    @Column(name = "reduction_yn", nullable = false)
    private Boolean reductionYn = false;

    @Column(name = "reduction_start")
    private LocalDate reductionStart;

    @Column(name = "reduction_end")
    private LocalDate reductionEnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CaseStatus status = CaseStatus.START;

    // IS NULL or  claim_from <= claim_to
    @AssertTrue(message = "claim_from must be <= claim_to")
    public boolean isClaimRangeValid() {
        return claimFrom == null || claimTo == null || claimFrom <= claimTo;
    }

    public TaxCase(Customer customer) {
        this.customer = customer;
    }
}
