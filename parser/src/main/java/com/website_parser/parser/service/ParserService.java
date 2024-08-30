package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.website_parser.parser.util.CssUtil.cssLinkToStyle;


@Service
@Slf4j
public class ParserService {

    private final WebDriverPool driverPool;
    private final CacheService cacheService;
    private Website website;

    ParserService(WebDriverPool driverPool, Website website, CacheService cacheService) {

        this.driverPool = driverPool;
        this.website = website;
        this.cacheService = cacheService;
        driverPool.addToPool();

    }

    public String getInitialHtmlFromUrl(String url) throws Exception {
        Website websiteCache = cacheService.getWebsiteCache(url);
        String htmlContent;
        if (websiteCache != null) {
            website = websiteCache;
            System.out.println("Cached pages:::   " + website.getPages().size());
            htmlContent = websiteCache.getInitialHtml();
        } else {
            WebDriver driver = driverPool.getDriverPool();
            htmlContent = retrievePage(url, driver);
            System.out.println("Browser is opened. Please perform the required actions manually and press any button when finished..");
            Scanner scanner = new Scanner(System.in); //TODO change it to FE pressing of enter
            String s = scanner.nextLine();
            if (!s.isEmpty()) {
               String driverPageSource = driver.getPageSource();
                htmlContent = driverPageSource.replaceAll("(?s)<header[^>]*>.*?</header>", "");
                htmlContent = cssLinkToStyle(htmlContent, new URL(url));
                website = Website.builder().websiteUrl(new URL(url)).initialHtml(htmlContent).pages(new HashMap<>()).build();
                cacheService.setWebsiteCache(url, website);
            }
            driverPool.releaseDriver(driver);
        }
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


}
