package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.cache.annotation.Cacheable;
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
    private final Website website;

    ParserService(WebDriverPool driverPool, Website website) {

        this.driverPool = driverPool;
        this.website = website;
        driverPool.addToPool();

    }

    @Cacheable("initialHtml")
    public String getInitialHtmlFromUrl(String url) throws MalformedURLException {
        website.setWebsiteUrl(new URL(url));
        WebDriver driver = driverPool.getDriverPool();
        //websiteUrl = new URL(url);
        String htmlContent = null;
        try {
            driver.get(url);
        } catch (WebDriverException e) {
            driver = driverPool.reconnectToBrowser(driver);
        }
        System.out.println("Browser is opened. Please perform the required actions manually and press any button when finished..");
        Scanner scanner = new Scanner(System.in); //TODO change it to FE pressing of enter
        String userInput = scanner.nextLine();
        if (!userInput.isEmpty()) {
            htmlContent = driver.getPageSource();
            htmlContent = htmlContent.replaceAll("(?s)<header[^>]*>.*?</header>", "");
        }
        return htmlContent;
    }


    private String getLastPageWithHost(String lastPage) {
        if (!lastPage.contains("http")) {
            return website.getWebsiteUrl().getProtocol() + "://" + website.getWebsiteUrl().getHost() + lastPage;
        }
        return lastPage;
    }

    @Cacheable("pages")
    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage) throws ExecutionException, InterruptedException {
        AtomicInteger successfulCount = new AtomicInteger(0);
        ArrayList<String> allPageUrls = UrlUtil.predictAllUrls(getLastPageWithHost(lastPage));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<List<String>> resultFuture = null;
        List<String> htmlsList = new ArrayList<>();
        if (allPageUrls != null) {
            for (String url : allPageUrls) {
                WebDriver driverMulti = driverPool.getDriverPool();
                CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
                    addPageToList(url, driverMulti, htmlsList);
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
                    log.error("error! -- {}", url);
                    return null;
                });
                futures.add(completableFuture);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            resultFuture = allOf.thenApply(v -> {
                website.setPages(htmlsList);
                log.info("All tasks completed. Total successful: {}", successfulCount.get());
                log.info("HTML list size: {}", htmlsList.size());
                return htmlsList;
            });
        }
        return resultFuture.get();
    }

    private void addPageToList(String url, WebDriver driver, List<String> htmlsList) {
        driver.get(url);
        System.out.println(url);
        htmlsList.add(driver.getPageSource());
    }
}
