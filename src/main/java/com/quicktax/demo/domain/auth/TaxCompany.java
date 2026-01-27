package com.quicktax.demo.domain.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tax_company")
@Getter
@Setter
@NoArgsConstructor
public class TaxCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpa_id")
    private Long cpaId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    public TaxCompany(String password) {
        this.password = password;
    }
}