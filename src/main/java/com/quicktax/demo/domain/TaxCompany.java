package com.quicktax.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tax_company")
@Getter
@NoArgsConstructor
public class TaxCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpa_id")
    private Long cpaId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public TaxCompany(String name) {
        this.name = name;
    }
}
