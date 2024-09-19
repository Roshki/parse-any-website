package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.website_parser.parser.util.CssUtil.cssLinksToStyleAndReturn;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParserService {

    private final WebDriverService webDriverService;
    private final CacheService cacheService;
    private final Website website;
    private final ApprovalService approvalService;


    public String getCachedPage(String url) {
        Website websiteCache = cacheService.getWebsiteCache(url);
        if (websiteCache != null) {
            populateWebsite(websiteCache);
            System.out.println("Cached pages:::   " + website.getPages().size());
            return websiteCache.getInitialHtml();
        } else {
            return null;
        }
    }

    private void populateWebsite(Website retrievedWebsite) {
        website.setPages(retrievedWebsite.getPages());
        website.setWebsiteUrl(retrievedWebsite.getWebsiteUrl());
        website.setInitialHtml(retrievedWebsite.getInitialHtml());
    }

    public String getNotCachedPage(String url) throws MalformedURLException {
        String htmlContent;
        // WebDriver initialDriver = driverPool.getNewChromeDriver();
        WebDriver initialDriver = webDriverService.getInitialDriver();
        retrievePage(url, initialDriver);
        try {
            long startTime = System.nanoTime();
            System.out.println("tries to approve");
            approvalService.getApprovalFuture().get(300, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            System.out.println("approves");
            long elapsedTime = endTime - startTime;
            double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
            System.out.printf("Time taken: %.3f seconds%n", elapsedTimeInSeconds);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Approved!!!");
        String driverPageSource = retrievePage(url, initialDriver);
        htmlContent = cssLinksToStyleAndReturn(driverPageSource, new URL(url))
                .replaceAll("(?s)<header[^>]*>.*?</header>", "")
                .replaceAll("z-index:\\s*\\d+", "")
                .replaceAll("(?s)<nav[^>]*>.*?</nav>", "");
        populateWebsite(new Website(url, htmlContent, new HashMap<>()));
        cacheService.setWebsiteCache(url, website);
//        initialDriver.close();
        approvalService.reset();

        return htmlContent;
    }

    public boolean isPageCached(String url) {
        return Optional.ofNullable(website)
                .map(Website::getPages)
                .map(pages -> pages.containsKey(url))
                .orElse(false);
    }

    public String retrievePage(String url, WebDriver driver) {
        try {
            driver.get(url);
            return driver.getPageSource();
        } catch (Exception e) {
            webDriverService.safelyCloseAndQuitDriver(driver);
            driver = webDriverService.getNewChromeDriver();
            driver.get(url);
            return driver.getPageSource();
        }
    }

}
