package com.zerobase.dividend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class CompanyController {

    // 배당금 검색 - 자동완성
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        return null;
    }

    // 회사 리스트 조회
    @GetMapping("")
    public ResponseEntity<?> searchCompany() {
        return null;
    }

    // 배당금 저장
    @PostMapping("")
    public ResponseEntity<?> addCompany() {
        return null;
    }

    // 배당금 삭제
    @DeleteMapping("")
    public ResponseEntity<?> deleteCompany(@RequestParam String ticker) {
        return null;
    }
}
