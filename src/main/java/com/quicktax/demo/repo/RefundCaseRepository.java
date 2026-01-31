package com.quicktax.demo.repo;

import com.quicktax.demo.domain.refund.RefundCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefundCaseRepository extends JpaRepository<RefundCase, Long> {
    // 특정 고객 ID와 연결된 모든 과거 기록을 찾는 메서드
    List<RefundCase> findByCustomer_CustomerId(Long customerId);
}