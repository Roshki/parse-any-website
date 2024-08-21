package com.website_parser.parser.service;

import com.website_parser.parser.util.UrlUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ParserService {

    private WebDriver driver;
    private final PaginationService paginationService;
    private final WebDriverPool driverPool;
    private static URL websiteUrl;

    ParserService(PaginationService paginationService, WebDriverPool driverPool) {
        this.paginationService = paginationService;
        this.driverPool = driverPool;
        System.setProperty("webdriver.chrome.driver", "chromedriver-mac-arm64/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        //options.setExperimentalOption("excludeSwitches",Collections.singletonList("enable-automation"));
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--disable-infobars");
//        options.setExperimentalOption("useAutomationExtension", false);
//        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
//        options.addArguments("--enable-automation");
//        options.addArguments("--disable-extensions");
//        options.addArguments("--disable-popup-blocking");
//        options.addArguments("--profile-directory=Default");
//        options.addArguments("--ignore-certificate-errors");
//        options.addArguments("--disable-plugins-discovery");
//        options.addArguments("--incognito");
//        options.addArguments("user_agent=DN");
        driver = new ChromeDriver(options);
    }

    public String getInitialHtmlFromUrl(String url) throws MalformedURLException {
        websiteUrl = new URL(url);
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


    public List<String> getHtmlOfAllPages(String paginationTag) {
        List<WebElement> pagination = paginationService.getPagination(paginationTag, driver);
        ArrayList<String> allPageUrls = UrlUtil.predictAllUrls(paginationService.getLastPage(pagination));
        List<String> htmlsList = new ArrayList<>();
        if (allPageUrls != null) {
            for (String url : allPageUrls) {
                driver.get(url);
                System.out.println(url);
                new WebDriverWait(driver, Duration.ofSeconds(40)).until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
                htmlsList.add(driver.getPageSource());
            }
        }
        return htmlsList;
    }

    public List<String> getHtmlOfAllPagesBasedOnLastPage_test(String lastPage) {

        ArrayList<String> allPageUrls = UrlUtil.predictAllUrls(getLastPageWithHost(lastPage));
        List<String> htmlsList = new ArrayList<>();
        if (allPageUrls != null) {
            for (String url : allPageUrls) {
                driver.get(url);
                System.out.println(url);
                new WebDriverWait(driver, Duration.ofSeconds(40)).until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
                htmlsList.add(driver.getPageSource());
            }
        }
        return htmlsList;
    }

    private String getLastPageWithHost(String lastPage) {
        if (!lastPage.contains("http")) {
            return websiteUrl.getProtocol() + "://" + websiteUrl.getHost() + lastPage;
        }
        return lastPage;
    }

    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage) throws ExecutionException, InterruptedException {
        driverPool.addToPool();
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
                    System.out.println("error!");
                    return null;
                });
                futures.add(completableFuture);
                //  driver.get(url);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            resultFuture = allOf.thenApply(v -> {
                System.out.println("All tasks completed. Total successful: " + successfulCount.get());
                System.out.println("HTML list size: " + htmlsList.size());
                return htmlsList; // Return the collected HTML strings
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
