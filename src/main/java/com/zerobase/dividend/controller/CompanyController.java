package com.zerobase.dividend.controller;

import com.zerobase.dividend.domain.CompanyEntity;
import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 배당금 검색 - 자동완성
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        return null;
    }

    /**
     * 회사 리스트 조회
     */
    @GetMapping("")
    public ResponseEntity<?> searchCompany() {
        List<CompanyEntity> companies = this.companyService.getAllCompany();
        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     */
    @PostMapping("")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) { // ticker 를 빈 값으로 입력한 경우, error 발생시킴
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);

        return ResponseEntity.ok(company);
    }

    // 배당금 삭제
    @DeleteMapping("")
    public ResponseEntity<?> deleteCompany(@RequestParam String ticker) {
        return null;
    }
}
