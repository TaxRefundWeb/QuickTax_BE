package com.quicktax.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "customer",
        uniqueConstraints = @UniqueConstraint(name = "uk_customer_cpa_rrn", columnNames = {"cpa_id", "rrn_enc"})
)
@Getter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpa_id", nullable = false)
    private TaxCompany taxCompany;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "rrn_enc", nullable = false, columnDefinition = "text")
    private String rrnEnc;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "account_number_enc", columnDefinition = "text")
    private String accountNumberEnc;

    public Customer(TaxCompany taxCompany, String name, String rrnEnc, String bankCode, String accountNumberEnc) {
        this.taxCompany = taxCompany;
        this.name = name;
        this.rrnEnc = rrnEnc;
        this.bankCode = bankCode;
        this.accountNumberEnc = accountNumberEnc;
    }
}
