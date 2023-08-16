package com.zerobase.dividend.service;

import com.zerobase.dividend.domain.MemberEntity;
import com.zerobase.dividend.dto.Auth;
import com.zerobase.dividend.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. -> " + username));
    }

    // 회원가입 기능
    public MemberEntity register(Auth.signUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) { // 동일한 id 가 존재하는 경우 에러 발생
            throw new RuntimeException("이미 사용 중인 아이디 입니다.");
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword())); // password 인코딩
        var result = this.memberRepository.save(member.toEntity());

        return result;
    }

    // 인증 기능
    public MemberEntity authenticate(Auth.signIn member) {
        return null;
    }

}
