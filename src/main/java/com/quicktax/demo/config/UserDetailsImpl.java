package com.quicktax.demo.config;

import com.quicktax.demo.domain.auth.TaxCompany; // 💡 회원님의 세무사 엔티티 임포트
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    // 💡 실제 DB의 세무사 정보 객체를 품고 있음
    private final TaxCompany taxCompany;

    // 편의 메서드: 세무사 ID(PK)를 바로 꺼낼 수 있게 함
    public Long getCpaId() {
        return taxCompany.getCpaId();
    }

    // --- 아래는 Spring Security 필수 구현 메서드 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 설정 (ROLE_USER, ROLE_ADMIN 등)
        // 현재는 단순하게 "ROLE_USER" 권한을 부여한다고 가정
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    @Override
    public String getPassword() {
        return taxCompany.getPassword();
    }

    @Override
    public String getUsername() {
        // 보통은 로그인 ID(이메일 등)를 반환하지만, 여기선 PK 문자열이나 이름을 반환
        return String.valueOf(taxCompany.getCpaId());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}