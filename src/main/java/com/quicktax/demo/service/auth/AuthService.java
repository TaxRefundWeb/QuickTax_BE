package com.quicktax.demo.service.auth;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.repo.TaxCompanyRepository;
import com.quicktax.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final TaxCompanyRepository taxCompanyRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder; // SecurityConfig의 BCrypt가 주입됨

    public String login(Long cpaId, String password) {
        // AuthService.java 내 login 메서드 상단
        String encoded = passwordEncoder.encode("1234");
        System.out.println("서버가 만든 진짜 암호문: " + encoded);

        System.out.println("현재 사용 중인 인코더: " + passwordEncoder.getClass().getSimpleName());

        TaxCompany company = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400, "ID 없음"));

        // 콘솔에 찍어서 눈으로 확인
        System.out.println("--- 로그인 디버깅 ---");
        System.out.println("입력된 비번: [" + password + "]");
        System.out.println("DB 저장된 값: [" + company.getPassword() + "]");
        System.out.println("비교 결과: " + passwordEncoder.matches(password, company.getPassword()));

        if (!passwordEncoder.matches(password.trim(), company.getPassword())) {
            throw new ApiException(ErrorCode.BADREQ400, "비밀번호 불일치");
        }
        return jwtUtil.generateToken(cpaId);
    }
}