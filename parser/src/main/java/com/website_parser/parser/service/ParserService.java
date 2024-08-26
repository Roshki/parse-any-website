package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;


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

    public String getInitialHtmlFromUrl(String url) throws MalformedURLException {
        Website websiteCache = cacheService.getWebsiteCache(url);
        if (websiteCache != null) {
            website = websiteCache;
            return websiteCache.getInitialHtml();
        } else {
            WebDriver driver = driverPool.getDriverPool();
            String htmlContent = queryPageByUrl(url, driver);
            String manualInput = getManualInput();
            if (!manualInput.isEmpty()) {
                htmlContent = htmlContent.replaceAll("(?s)<header[^>]*>.*?</header>", "");
                website = Website.builder().websiteUrl(new URL(url)).initialHtml(htmlContent).build();
                cacheService.setWebsiteCache(url, website);
            }
            return website.getInitialHtml();
        }

    }


    private String getLastPageWithHost(String lastPage) {
        if (!lastPage.contains("http")) {
            return website.getWebsiteUrl().getProtocol() + "://" + website.getWebsiteUrl().getHost() + lastPage;
        }
        return lastPage;
    }

    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage) throws ExecutionException, InterruptedException {
        AtomicInteger successfulCount = new AtomicInteger(0);
        ArrayList<String> allPageUrls = UrlUtil.predictAllUrls(getLastPageWithHost(lastPage));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<List<String>> resultFuture = null;
        Map<String, String> htmlPagesMap = new HashMap<>();
        if (allPageUrls != null) {
            for (String url : allPageUrls) {
                WebDriver driverMulti = driverPool.getDriverPool();

                CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
                    if (!isPageCached(url)) {
                        // Page is not in the cache, so we retrieve it via WebDriver
                        String htmlPage = queryPageByUrl(url, driverMulti);
                        htmlPagesMap.put(url, htmlPage);
                        cacheService.setWebsiteCache(website.getWebsiteUrl().toString(), website);
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
                website.setPages(htmlPagesMap);
                log.info("All tasks completed. Total successful: {}", successfulCount.get());
                log.info("HTML list size: {}", website.getPages().entrySet().size());
                return website.getPages().values().stream().toList();
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

    private String queryPageByUrl(String url, WebDriver driver) {
        try {
            driver.get(url);
        } catch (WebDriverException e) {
            driver = driverPool.reconnectToBrowser(driver);
        }
        System.out.println(url);
        return driver.getPageSource();

    }

    private static String getManualInput() {
        System.out.println("Browser is opened. Please perform the required actions manually and press any button when finished..");
        Scanner scanner = new Scanner(System.in); //TODO change it to FE pressing of enter
        return scanner.nextLine();
    }

}
