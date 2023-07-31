package com.zerobase.dividend.scraper;

import com.zerobase.dividend.dto.Company;
import com.zerobase.dividend.dto.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);

}
