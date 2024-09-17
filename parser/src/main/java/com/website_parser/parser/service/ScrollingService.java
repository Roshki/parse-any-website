package com.website_parser.parser.service;

import com.website_parser.parser.util.CssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrollingService {

    private final WebDriverPool driverPool;
    private final ApprovalService approvalService;
    private static final String URL_scroll_linkedin = "https://www.linkedin.com/jobs/search?keywords=&location=Netherlands&geoId=102890719&f_TPR=r86400&position=1&pageNum=0";
    private static final String URL_scroll_marktplaats = "https://www.marktplaats.nl/";


    public String getInfiniteScrolling(String url) throws ExecutionException, InterruptedException, TimeoutException, MalformedURLException {
        int timesOfScrolling = 0;
        WebDriver driver = driverPool.getChromeDriver();
        driver.get(url);
        long startTime = System.nanoTime();
        System.out.println("tries to approve");
        approvalService.getApprovalFuture().get(300, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        System.out.println("approves");
        long elapsedTime = endTime - startTime;
        double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
        System.out.printf("Time taken: %.3f seconds%n", elapsedTimeInSeconds);
        List<String> seenButtons = getElementIdentifiers(driver.findElements(By.cssSelector("button")));
        long lastHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        while (true) {
            timesOfScrolling++;
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight-1000);");

            Thread.sleep(2000);

//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//            long finalLastHeight = lastHeight;
//            try {
//                wait.until(d -> {
//                    long newHeight = (long) ((JavascriptExecutor) d).executeScript("return document.body.scrollHeight");
//                    return newHeight != finalLastHeight;
//                });
//            } catch (org.openqa.selenium.TimeoutException ex) {
//                log.warn("height is same");
//            }
            List<WebElement> currentButtons = driver.findElements(By.cssSelector("button"));

            for (WebElement button : currentButtons) {
                String currentButtonId = getElementIdentifier(button);
                if (!seenButtons.contains(currentButtonId)) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        System.out.println("New button found and clicked: " + button.getText());
                        try {
                            button.click();
                        } catch (Exception e) {
                            log.warn("button is not clickable because of: {}", e.getMessage());
                        }
                        break;
                    }
                }
            }
            long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
            Thread.sleep(2000);
            System.out.println(lastHeight + " - " + newHeight);
            if (newHeight == lastHeight) {
                System.out.println(timesOfScrolling);
                break;
            }
            lastHeight = newHeight;
        }
        String htmlContent = driver.getPageSource();
        CssUtil.cssLinkToStyle(htmlContent, new URL(url));
        driver.close();
        driver.quit();
        return htmlContent;
    }

    private List<String> getElementIdentifiers(List<WebElement> elements) {
        return elements.stream()
                .map(this::getElementIdentifier)
                .collect(Collectors.toList());
    }

    private String getElementIdentifier(WebElement element) {
        return element.getTagName() + ":" + element.getAttribute("id") + ":" + element.getText();
    }
}
