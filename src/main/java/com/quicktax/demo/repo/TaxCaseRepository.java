package com.quicktax.demo.repo;

import com.quicktax.demo.domain.cases.TaxCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxCaseRepository extends JpaRepository<TaxCase, Long> {
    Optional<TaxCase> findByCaseIdAndCustomer_TaxCompany_CpaId(Long caseId, Long cpaId);

    List<TaxCase> findByCustomer_CustomerIdAndCustomer_TaxCompany_CpaId(Long customerId, Long cpaId);
}
