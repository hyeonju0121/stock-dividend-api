package com.zerobase.dividend.service;

import com.zerobase.dividend.domain.CompanyEntity;
import com.zerobase.dividend.domain.DividendEntity;
import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.dto.ScrapedResult;
import com.zerobase.dividend.exception.impl.NoCompanyException;
import com.zerobase.dividend.repository.CompanyRepository;
import com.zerobase.dividend.repository.DividendRepository;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker); // 해당 ticker 가 DB에 존재하는 경우, error 발생시킴
        }
        return this.storeCompanyAndDividend(ticker); // DB에 존재하지 않는 경우, 스크래핑하는 메서드 호출
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    // 해당 ticker 로 메타정보와 배당금 정보를 스크래핑하고, DB에 저장하는 메서드
    private Company storeCompanyAndDividend(String ticker) {
        // ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) { // 해당 ticker 가 존재하지 않을 경우, error 발생시킴
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company)); // Company DB에 메타정보 저장
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                                                            .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                            .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities); // Dividend DB에 배당금 정보 저장

        return company;
    }

    // LIKE 연산자를 사용해 키워드에 해당하는 회사명을 조회하는 메서드
    public Page<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

        List<String> result = companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());

        return new PageImpl<>(result);
    }

    // trie 에 회사명을 저장하는 메서드
    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    // trie 에서 키워드에 해당하는 회사명을 조회하는 메서드
    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }

    // trie 에 저장된 키워드를 삭제하는 메서드
    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                                        .orElseThrow(() -> new NoCompanyException()); // ticker 명이 존재하지 않다면 error 발생

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName()); // trie 에 있는 회사 키워드 삭제

        return company.getName();
    }
}
