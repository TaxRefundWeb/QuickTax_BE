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

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    public TaxCompany(String password) {
        this.password = password;
    }
}

