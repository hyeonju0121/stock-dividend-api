package com.zerobase.dividend.controller;

import com.zerobase.dividend.domain.CompanyEntity;
import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.dto.constants.CacheKey;
import com.zerobase.dividend.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    /**
     * 자동완성
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        var result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    /**
     * 회사 리스트 조회
     */
    @GetMapping("")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     */
    @PostMapping("")
    @PreAuthorize("hasRole('WRITE')") // 쓰기 권한이 있는 USER 만 API 호출 가능
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) { // ticker 를 빈 값으로 입력한 경우, error 발생시킴
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(company.getName()); // trie 에 회사명 추가

        return ResponseEntity.ok(company);
    }

    /**
     * 회사 삭제
     */
    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')") // 관리 권한이 있는 user 만 api 호출 가능
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);

        return ResponseEntity.ok(companyName);
    }

    // 캐시에 존재하는 회사 데이터 삭제
    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
