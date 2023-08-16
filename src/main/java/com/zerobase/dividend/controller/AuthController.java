package com.zerobase.dividend.controller;

import com.zerobase.dividend.dto.Auth;
import com.zerobase.dividend.security.TokenProvider;
import com.zerobase.dividend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    // 회원가입 api
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result =  this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인 api
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        // 패스워드 검증
        var member = this.memberService.authenticate(request);
        // 토큰 생성
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());

        log.info("user login -> " + request.getUsername());
        return ResponseEntity.ok(token);
    }
}
