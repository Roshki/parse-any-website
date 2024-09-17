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

import static com.website_parser.parser.util.CssUtil.cssLinkToStyle;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParserService {

    private final WebDriverPool driverPool;
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
        WebDriver driver = driverPool.getChromeDriver();
        retrievePage(url, driver);
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
        String driverPageSource = driver.getPageSource();
        htmlContent = driverPageSource.replaceAll("(?s)<header[^>]*>.*?</header>", "");
        htmlContent = cssLinkToStyle(htmlContent, new URL(url));
        populateWebsite(new Website(url, htmlContent, new HashMap<>()));
        cacheService.setWebsiteCache(url, website);
        driver.close();
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
        } catch (WebDriverException e) {
            driver = driverPool.reconnectToBrowser(driver);
            driver.get(url);
        }
        System.out.println(url);
        return driver.getPageSource();

    }

}
