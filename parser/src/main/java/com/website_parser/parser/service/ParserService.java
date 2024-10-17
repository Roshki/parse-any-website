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

import static com.website_parser.parser.util.HtmlContentUtil.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParserService {

    private final WebDriverPoolService webDriverService;
    private final CacheService cacheService;
    private final Website website;
    private final ApprovalService approvalService;


    public String getCachedPage(String url) {
        Website websiteCache = cacheService.getWebsiteCache(url);
        if (websiteCache != null) {
            website.populateWebsite(websiteCache);
            System.out.println("Cached pages:::   " + website.getPages().size());
            return websiteCache.getInitialHtml();
        } else {
            return null;
        }
    }

    public String getNotCachedPage(String url) throws MalformedURLException {
        String htmlContent;
        WebDriver initialDriver = webDriverService.getDriverFromPool();
        initialDriver.get(url);
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
        } finally {
            webDriverService.releaseDriverToThePool(initialDriver);
            approvalService.reset();
        }
        System.out.println("Approved!!!");
        String driverPageSource = retrievePage(initialDriver);
        htmlContent = updateHtmlAndReturn(driverPageSource, new URL(url));
        website.populateWebsite(
                Website.builder()
                        .websiteUrl(url)
                        .initialHtml(htmlContent).build());
        cacheService.setWebsiteCache(url, website);
        return htmlContent;
    }

    public boolean isPageCached(String url) {
        return Optional.ofNullable(website)
                .map(Website::getPages)
                .map(pages -> pages.containsKey(url))
                .orElse(false);
    }

    public String retrievePage(WebDriver driver) {
        return driver.getPageSource();
    }

    public String ifWebDriverConn() {
        try {
            WebDriver w = webDriverService.getDriverFromPool();
            w.get("https://www.google.com/");
            webDriverService.releaseDriverToThePool(w);
        } catch (Exception e) {
            log.error("not able to connect!!!");
        }
        return "OK!";
    }

    public String getCleanHtml(Website website) throws MalformedURLException {
        this.website.populateWebsite(
                Website.builder()
                        .websiteUrl(website.getWebsiteUrl())
                        .initialHtml(website.getInitialHtml()).build());
        return updateHtmlAndReturn(website.getInitialHtml(), new URL(website.getWebsiteUrl()));
    }
}
