package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaginationService {

    private final ParserService parserService;
    private final CacheService cacheService;
    private final WebDriverPoolService webDriverService;
    private final Website website;
    private final SseEmitterService sseEmitterService;
    private final UserService userService;


    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage, String pageTageName, String pageStart, String pageFinish, String guid) throws ExecutionException, InterruptedException {
        List<String> allPageUrls = UrlUtil.predictAllUrls(lastPage, pageTageName, pageStart, pageFinish);
        AtomicInteger successfulCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<List<String>> resultFuture;
        Map<String, String> htmlPagesMap = new HashMap<>();
        for (String url : allPageUrls) {
            WebDriver driverMulti = webDriverService.getDriverFromPool();
            int count = successfulCount.incrementAndGet();
            CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
                if (!parserService.isPageCached(url)) {
                    // Page is not in the cache, so we retrieve it via WebDriver
                    driverMulti.get(url);
                    String htmlPage = driverMulti.getPageSource();
                    htmlPagesMap.put(url, htmlPage);
                } else {
                    htmlPagesMap.put(url, website.getPages().get(url));
                }
                return null;
            }).thenAccept(result -> {
                System.out.println("success -- " + url);
                webDriverService.releaseDriverToThePool(driverMulti);
            }).exceptionally(ex -> {
                if (driverMulti != null) {
                    webDriverService.releaseDriverToThePool(driverMulti);
                }
                log.error("error! -- {}", url, ex.getCause());
                webDriverService.safelyCloseAndQuitDriver(driverMulti);
                userService.processByGuidInQueue(guid);
                sseEmitterService.completeSseWithError(ex, guid);
                return null;
            });
            futures.add(completableFuture);
            sseEmitterService.sendSse(String.valueOf((100 * count) / allPageUrls.size()), "progress" + guid);
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        resultFuture = allOf.thenApply(v -> {
            website.setPages(htmlPagesMap);
            cacheService.setWebsiteCache(website.getWebsiteUrl(), website);
            log.info("All tasks completed. Total successful: {}", successfulCount.get());
            log.info("HTML list size: {}", website.getPages().entrySet().size());
            userService.processFirstInQueue();
            return htmlPagesMap.values().stream().toList();
        });
        return resultFuture != null ? resultFuture.get() : null;
    }
}
