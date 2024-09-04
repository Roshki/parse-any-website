package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.website_parser.parser.util.CssUtil.cssLinkToStyle;


@Service
@Slf4j
public class ParserService {

    private final WebDriverPool driverPool;
    private final CacheService cacheService;
    private Website website;
    private final ApprovalService approvalService;

    ParserService(WebDriverPool driverPool, Website website, CacheService cacheService, ApprovalService approvalService) {
        this.driverPool = driverPool;
        this.website = website;
        this.cacheService = cacheService;
        this.approvalService = approvalService;
        driverPool.addToPool();
    }

    public String getCachedPage(String url) throws Exception {
        Website websiteCache = cacheService.getWebsiteCache(url);
        if (websiteCache != null) {
            website = websiteCache;
            System.out.println("Cached pages:::   " + website.getPages().size());
            return websiteCache.getInitialHtml();
        } else {
            return null;
//            WebDriver driver = driverPool.getDriverPool();
//            retrievePage(url, driver);
//            return getHtml(url, driver);
        }
    }

    public String getNotCachedPage(String url) throws MalformedURLException {
        String htmlContent;
        WebDriver driver = driverPool.getDriverPool();
        retrievePage(url, driver);
        try {
            long startTime = System.nanoTime();
            System.out.println("tries to approve");
            approvalService.getApprovalFuture().get(30, TimeUnit.SECONDS);
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
        website = Website.builder().websiteUrl(new URL(url)).initialHtml(htmlContent).pages(new HashMap<>()).build();
        cacheService.setWebsiteCache(url, website);
        driverPool.releaseDriver(driver);
        approvalService.reset();

        return htmlContent;
    }

    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage) throws ExecutionException, InterruptedException {
        AtomicInteger successfulCount = new AtomicInteger(0);
        ArrayList<String> allPageUrls = UrlUtil.predictAllUrls(UrlUtil.verifyHost(lastPage, website.getWebsiteUrl()));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<List<String>> resultFuture = null;
        Map<String, String> htmlPagesMap = new HashMap<>();
        if (allPageUrls != null) {
            for (String url : allPageUrls) {
                WebDriver driverMulti = driverPool.getDriverPool();

                CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
                    if (!isPageCached(url)) {
                        // Page is not in the cache, so we retrieve it via WebDriver
                        String htmlPage = retrievePage(url, driverMulti);
                        htmlPagesMap.put(url, htmlPage);
                    } else {
                        htmlPagesMap.put(url, website.getPages().get(url));
                    }
                    return null;
                }).thenAccept(result -> {
                    int count = successfulCount.incrementAndGet();
                    System.out.println("success -- " + url);
                    driverPool.releaseDriver(driverMulti);
                    //emit sse
                }).exceptionally(ex -> {
                    if (driverMulti != null) {
                        driverPool.releaseDriver(driverMulti);
                    }
                    log.error("error! -- {}", url, ex.getCause());
                    return null;
                });
                futures.add(completableFuture);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            resultFuture = allOf.thenApply(v -> {
                website.getPages().putAll(htmlPagesMap);
                cacheService.setWebsiteCache(website.getWebsiteUrl().toString(), website);
                log.info("All tasks completed. Total successful: {}", successfulCount.get());
                log.info("HTML list size: {}", website.getPages().entrySet().size());
                return htmlPagesMap.values().stream().toList();
            });
        }
        return resultFuture.get();
    }

    private boolean isPageCached(String url) {
        return Optional.ofNullable(website)
                .map(Website::getPages)
                .map(pages -> pages.containsKey(url))
                .orElse(false);
    }

    private String retrievePage(String url, WebDriver driver) {
        try {
            driver.get(url);
        } catch (WebDriverException e) {
            driver = driverPool.reconnectToBrowser(driver);
            driver.get(url);
        }
        System.out.println(url);
        return driver.getPageSource();

    }

    public void ifPageConfirmed(boolean ifConfirmed) {
        website.setIfConfirmed(ifConfirmed);
    }


}
