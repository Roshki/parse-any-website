package com.website_parser.parser.service;

import com.website_parser.parser.util.BannersUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrollingService {

    private final WebDriverService driverPool;

    // @TODO parameters: speed, pauses, amount of scrolls
    // @TODO handling of iframe,section
    public String getInfiniteScrolling(String url) throws InterruptedException {
        int timesOfScrolling = 0;
        WebDriver driver = driverPool.verifyInitialDriver();
        driver.get(url);
        List<String> seenButtons = getElementIdentifiers(driver.findElements(By.cssSelector("button")));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        final long[] lastHeight = {(long) jsExecutor.executeScript("return document.body.scrollHeight")};
        while (true) {
            timesOfScrolling++;
            Thread.sleep(1000);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            BannersUtil.handleBannerIfPresent(driver);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("sp_message_iframe_1177873")));
                System.out.println("ok");
                System.out.println(driver.getPageSource());
                wait.until(driver1 -> {
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight+500);");
                    long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
                    System.out.println(newHeight);
                    return newHeight > lastHeight[0];
                });
            } catch (Exception e) {
                log.warn("Page didn't load or timeout occurred", e);
            }

            List<WebElement> currentButtons = driver.findElements(By.cssSelector("button"));

//            for (WebElement button : currentButtons) {
//                String currentButtonId = getElementIdentifier(button);
//                if (!seenButtons.contains(currentButtonId)) {
//                    if (button.isDisplayed() && button.isEnabled()) {
//                        System.out.println("New button found and clicked: " + button.getText());
//                        try {
//                            button.click();
//                        } catch (Exception e) {
//                            log.warn("button is not clickable because of: {}", e.getMessage());
//                        }
//                        break;
//                    }
//                }
//            }
            long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
            System.out.println(lastHeight[0] + " - " + newHeight);
            if (newHeight == lastHeight[0] || timesOfScrolling == 5) {
                System.out.println("Scroll limit reached: " + timesOfScrolling);
                break;
            }
            lastHeight[0] = newHeight;
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
