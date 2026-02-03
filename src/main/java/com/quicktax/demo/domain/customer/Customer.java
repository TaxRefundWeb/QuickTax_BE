package com.quicktax.demo.domain.customer;

import com.quicktax.demo.domain.auth.TaxCompany;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "customer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_customer_cpa_rrn",
                columnNames = {"cpa_id", "rrn"}
        ),
        indexes = {
                @Index(name = "idx_customer_cpa_id", columnList = "cpa_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpa_id", nullable = false)
    private TaxCompany taxCompany;

    @NotBlank
    @Size(max = 20)
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Size(max = 200)
    @Column(name = "address", length = 200)
    private String address;

    @NotBlank
    @Column(name = "rrn", nullable = false, columnDefinition = "text")
    private String rrn;

    @Size(max = 20)
    @Column(name = "bank", length = 20)
    private String bank;

    @Column(name = "bank_number", columnDefinition = "text")
    private String bankNumber;

    @NotBlank
    @Size(max = 3)
    @Column(name = "nationality_code", nullable = false, length = 3)
    private String nationalityCode;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nationality_name", nullable = false, length = 50)
    private String nationalityName;

    // 💡 [변경] Integer -> String으로 변경
    // 숫자 범위 제한(@Min, @Max) 삭제 (문자열에는 적용 불가)
    // 대신 길이를 넉넉하게 20자 정도로 설정 (예: "10%", "협의", "3.3" 등 수용)
    @Size(max = 20)
    @Column(name = "final_fee_percent", nullable = false, length = 20)
    private String finalFeePercent;

    public Customer(
            TaxCompany taxCompany,
            String name,
            String address,
            String rrn,
            String bank,
            String bankNumber,
            String nationalityCode,
            String nationalityName,
            String finalFeePercent // 💡 [변경] 매개변수 타입 String
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

    /**
     * 고객 기본 정보 업데이트 (조회 후 수정 시 사용)
     */
    public void updateBasicInfo(String address, String bank, String bankNumber, String finalFeePercent) { // 💡 [변경] 매개변수 타입 String
        this.address = address;
        this.bank = bank;
        this.bankNumber = bankNumber;
        this.finalFeePercent = finalFeePercent;
    }
}