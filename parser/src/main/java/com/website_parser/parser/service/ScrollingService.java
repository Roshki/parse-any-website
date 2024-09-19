package com.website_parser.parser.service;

import com.website_parser.parser.util.CssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrollingService {

    private final WebDriverService driverPool;

    // @TODO parameters: speed, pauses, amount of scrolls
    public String getInfiniteScrolling(String url) throws InterruptedException, MalformedURLException {
        int timesOfScrolling = 0;
        WebDriver driver = driverPool.getInitialDriver();
        driver.get(url);
        List<String> seenButtons = getElementIdentifiers(driver.findElements(By.cssSelector("button")));
        long lastHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        while (true) {
            timesOfScrolling++;
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight-1000);");

            Thread.sleep(2000);

//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));
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
            Thread.sleep(2000);
            long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
            System.out.println(lastHeight + " - " + newHeight);
            if (newHeight == lastHeight) {
                System.out.println(timesOfScrolling);
                break;
            }
            if (timesOfScrolling == 5) {
                System.out.println(timesOfScrolling);
                break;
            }
            lastHeight = newHeight;
        }
        String htmlContent = driver.getPageSource();
       // CssUtil.cssLinksToStyleAndReturn(htmlContent, new URL(url));
        driverPool.safelyCloseAndQuitDriver(driver);
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
