package com.zerobase.dividend.scraper;

import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.dto.Dividend;
import com.zerobase.dividend.dto.ScrapedResult;
import com.zerobase.dividend.dto.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";

    private static final long START_TIME = 86400; // 60초 * 60분 * 24시간

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000; // 1970년 1월 1일 ~ 현재 날짜 시간의 값 -> 초로 변경

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); // table 전체

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();

                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                // 연, 월, 일, 배당금 내역 추출
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) { // Month enum 에 정의되지 않은 값이 들어온 경우 error 발생시킴
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(Dividend.builder()
                                    .date(LocalDateTime.of(year, month, day, 0, 0)) // 연, 월, 일 -> LocalDateTime 타입으로 변경
                                    .dividend(dividend).
                                    build());

            }
            scrapResult.setDividendEntities(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    // 입력한 ticker 를 대상으로, 메타 정보를 스크래핑하여 Company 객체를 생성하는 메서드 (ticker, name)
    @Override
    public Company scrapCompanyByTicker(String ticker) {
        try {
            String url = String.format(SUMMARY_URL, ticker);
            Document document = Jsoup.connect(url).get();

            Element title = document.getElementsByTag("h1").get(0);

            String companyName = title.text();
            companyName = companyName.split(" - ")[1].trim();

            return Company.builder()
                            .ticker(ticker)
                            .name(companyName)
                            .build();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
