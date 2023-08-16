package com.zerobase.dividend.repository;

import com.zerobase.dividend.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByUsername(String username); // id 를 기준으로 회원정보 찾을 때 사용

    boolean existsByUsername(String username); // 회원가입을 할 때, id 존재 여부 확인할 때 사용
}
