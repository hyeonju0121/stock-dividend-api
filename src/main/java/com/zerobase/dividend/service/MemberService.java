package com.zerobase.dividend.service;

import com.zerobase.dividend.domain.MemberEntity;
import com.zerobase.dividend.dto.Auth;
import com.zerobase.dividend.exception.impl.AlreadyExistUserException;
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
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) { // 동일한 id 가 존재하는 경우 에러 발생
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword())); // password 인코딩
        var result = this.memberRepository.save(member.toEntity());

        return result;
    }

    // 패스워드 인증 기능
    public MemberEntity authenticate(Auth.SignIn member) {
        var user = this.memberRepository.findByUsername(member.getUsername())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        // 현재, user 에는 인코딩된 패스워드가 들어가있음
        // 따라서 입력으로 들어온 member 에 패스워드를 인코딩해서 user 패스워드와 비교
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
}
