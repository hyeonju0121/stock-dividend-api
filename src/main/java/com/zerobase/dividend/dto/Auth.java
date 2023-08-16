package com.zerobase.dividend.dto;

import com.zerobase.dividend.domain.MemberEntity;
import lombok.Data;

import java.util.List;

public class Auth {

    @Data
    public static class SignIn {
        private String username;
        private String password;
    }

    @Data
    public static class SignUp {
        private String username;
        private String password;
        private List<String> roles; // 권한 설정

        public MemberEntity toEntity() {
            return MemberEntity.builder()
                            .username(this.username)
                            .password(this.password)
                            .roles(this.roles)
                            .build();
        }
    }
}
