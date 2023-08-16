package com.zerobase.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
    private static final String KEY_ROLES = "roles";

    @Value("{spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     * @param username
     * @param roles
     * @return
     */
    public String generateToken(String username, List<String> roles) {
        // 사용자의 권한 정보를 저장하기 위한 Claims 생성
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles); // claims 에 데이터를 저장할 때는 key, value 타입으로 저장

        var now = new Date(); // 토큰이 생성된 시간
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME); // 토큰 만료 시간

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
                .compact();
    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false; // 토큰의 값이 빈 값이라면 유효하지 않기 때문에 false

        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }


    /**
     * 토큰으로 부터 Claims 정보 가져오기
     * @param token
     * @return
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) { // 이미 만료된 토큰을 파싱하려고 하는 경우, ExpiredJwtException 발생
            return e.getClaims();
        }

    }
}
