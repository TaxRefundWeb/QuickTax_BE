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
    private final PasswordEncoder passwordEncoder;

    public String login(Long cpaId, String password) {
        // 1. 사용자 조회
        TaxCompany company = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400, "존재하지 않는 사용자입니다."));

        // 2. 비밀번호 검증 (디버깅용 로그 제거하고 깔끔하게)
        if (!passwordEncoder.matches(password, company.getPassword())) {
            throw new ApiException(ErrorCode.BADREQ400, "비밀번호가 일치하지 않습니다.");
        }

        // 3. 💡 수정됨: 이메일 없이 ID만으로 토큰 생성 호출
        return jwtUtil.createToken(company.getCpaId());
    }
}