package com.quicktax.demo.repo;

import com.quicktax.demo.domain.auth.TaxCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxCompanyRepository extends JpaRepository<TaxCompany, Long> {
    // 기본 제공되는 findById를 통해 cpa_id 조회가 가능합니다.
}