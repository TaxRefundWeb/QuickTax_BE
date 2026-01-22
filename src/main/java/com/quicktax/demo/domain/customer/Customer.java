package com.quicktax.demo.domain;

import com.quicktax.demo.domain.auth.TaxCompany;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "customer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_customer_cpa_rrn",
                columnNames = {"cpa_id", "rrn"}
        )
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

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "rrn", nullable = false, columnDefinition = "text")
    private String rrn;

    @Column(name = "bank", length = 20)
    private String bank;

    @Column(name = "bank_number", columnDefinition = "text")
    private String bankNumber;

    @Column(name = "nationality_code", nullable = false, length = 3)
    private String nationalityCode;

    @Column(name = "nationality_name", nullable = false, length = 50)
    private String nationalityName;

    @Column(name = "final_fee_percent", nullable = false)
    private Integer finalFeePercent;

    public Customer(
            TaxCompany taxCompany,
            String name,
            String address,
            String rrn,
            String bank,
            String bankNumber,
            String nationalityCode,
            String nationalityName,
            Integer finalFeePercent
    ) {
        this.taxCompany = taxCompany;
        this.name = name;
        this.address = address;
        this.rrn = rrn;
        this.bank = bank;
        this.bankNumber = bankNumber;
        this.nationalityCode = nationalityCode;
        this.nationalityName = nationalityName;
        this.finalFeePercent = finalFeePercent;
    }
}
