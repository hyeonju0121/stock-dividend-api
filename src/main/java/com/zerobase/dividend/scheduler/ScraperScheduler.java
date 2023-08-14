package com.zerobase.dividend.scheduler;

import com.zerobase.dividend.domain.CompanyEntity;
import com.zerobase.dividend.domain.DividendEntity;
import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.dto.ScrapedResult;
import com.zerobase.dividend.repository.CompanyRepository;
import com.zerobase.dividend.repository.DividendRepository;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    // 일정 주기마다 수행
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started. ");
        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is started. -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(Company.builder()
                                                                                .ticker(company.getTicker())
                                                                                .name(company.getName())
                                                                                .build());

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // Dividend dto 를 DividendEntity 로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 순회하면서, 해당 배당금 데이터가 존재하지 않는 경우에 Dividend Repository 에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3000 -> 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}