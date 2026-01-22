package com.quicktax.demo.repo;

import com.quicktax.demo.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTaxCompany_CpaId(Long cpaId);
}
