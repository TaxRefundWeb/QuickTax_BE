package com.quicktax.demo.domain.cases;

import com.quicktax.demo.domain.customer.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
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

    @Column(name = "reduction_start")
    private LocalDate reductionStart;

    @Column(name = "reduction_end")
    private LocalDate reductionEnd;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @AssertTrue(message = "claim_from must be <= claim_to")
    public boolean isClaimRangeValid() {
        return claimFrom == null || claimTo == null || claimFrom <= claimTo;
    }

    public TaxCase(Customer customer) {
        this.customer = customer;
    }

    public TaxCase(Customer customer,
                   Integer claimFrom,
                   Integer claimTo,
                   LocalDate reductionStart,
                   LocalDate reductionEnd,
                   LocalDate claimDate) {
        this.customer = customer;
        this.claimFrom = claimFrom;
        this.claimTo = claimTo;
        this.reductionStart = reductionStart;
        this.reductionEnd = reductionEnd;
        this.claimDate = claimDate;
    }

    public void applySelection(Integer claimFrom,
                               Integer claimTo,
                               LocalDate reductionStart,
                               LocalDate reductionEnd,
                               LocalDate claimDate) {
        this.claimFrom = claimFrom;
        this.claimTo = claimTo;
        this.reductionStart = reductionStart;
        this.reductionEnd = reductionEnd;
        this.claimDate = claimDate;
    }

    public void applyClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }
}
