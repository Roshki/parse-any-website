package com.website_parser.parser.service;

import com.website_parser.parser.util.UrlUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;


@Service
public class ParserService {

    private static WebDriver driver;
    private final PaginationService paginationService;
    private static URL websiteUrl;

    ParserService(PaginationService paginationService) {
        this.paginationService = paginationService;
        System.setProperty("webdriver.chrome.driver", "chromedriver-mac-arm64/chromedriver");
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        //options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        driver = new ChromeDriver(options);
    }

    public String getInitialHtmlFromUrl(String url) throws MalformedURLException {
        websiteUrl = new URL(url);
        String htmlContent = null;
        driver.get(url);
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

    public List<String> getHtmlOfAllPagesBasedOnLastPage(String lastPage) {

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

}
