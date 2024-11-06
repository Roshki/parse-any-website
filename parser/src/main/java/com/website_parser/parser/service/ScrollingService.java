package com.website_parser.parser.service;

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

import static com.website_parser.parser.util.BannersUtil.handleBannerIfPresent;
import static com.website_parser.parser.util.HtmlContentUtil.updateHtmlAndReturn;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrollingService {

    private final WebDriverPoolService driverPool;
    private final SseEmitterService sseEmitterService;
    private final UserService userService;

    private static final String returnPageHeightScript = "return document.body.scrollHeight";
    private static final String reasonForMessage = "progress";

    public String getInfiniteScrolling(String url, String speed, int amount, String guid) throws MalformedURLException {
        int timesOfScrolling = 0;
        WebDriver driver = driverPool.getDriverFromPool();
        driver.get(url);
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driverPool.verifyAndGetWebDriver(driver);
        final long[] lastHeight = {(long) jsExecutor.executeScript(returnPageHeightScript)};
        handleBannerIfPresent(driver);
        sseEmitterService.sendSse("10", reasonForMessage + guid);
        while (true) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
            long theHeight;
            try {
                wait.until(driver1 -> {
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight-" + speed + ");");
                    long newHeight = (long) jsExecutor.executeScript(returnPageHeightScript);
                    System.out.println(newHeight);
                    return newHeight > lastHeight[0];
                });
                theHeight = (long) jsExecutor.executeScript(returnPageHeightScript);
            } catch (Exception e) {
                log.warn("Page could load only {} times for now.", timesOfScrolling);
                sseEmitterService.sendSse("Page could load only " + timesOfScrolling + " times for now. Page could be blocked or try to use another speed", reasonForMessage + guid);
                String pageSource = driver.getPageSource();
                driverPool.releaseDriverToThePool(driver);
                userService.processByGuidInQueue(guid);
                return updateHtmlAndReturn(pageSource, new URL(url));
            }
            System.out.println(lastHeight[0] + " - " + theHeight);
            if (theHeight == lastHeight[0] || timesOfScrolling == amount) {
                System.out.println("Scroll limit reached: " + timesOfScrolling);
                break;
            }
            lastHeight[0] = theHeight;

            timesOfScrolling++;
            sseEmitterService.sendSse(String.valueOf(((90L * timesOfScrolling) / amount) + 10), reasonForMessage + guid);
        }
        String pageSource = driver.getPageSource();
        driverPool.releaseDriverToThePool(driver);
        userService.processFirstInQueue();
        return updateHtmlAndReturn(pageSource, new URL(url));
    }

    private void pressButtonIfPreventsScrolling(WebDriver driver, List<String> seenButtons) {
        log.info("trying look for buttons which prevent scrolling...");
        List<WebElement> currentButtons = driver.findElements(By.cssSelector("button"));

        for (WebElement button : currentButtons) {
            String currentButtonId = getElementIdentifier(button);
            if (!seenButtons.contains(currentButtonId) && button.isDisplayed() && button.isEnabled()) {
                System.out.println("New button found: " + button.getText());
                try {
                    button.click();
                } catch (Exception e) {
                    log.warn("button is not clickable because of: {}", e.getMessage());
                }
                break;
            } else {
                log.warn("No buttons found, possibly the page is blocked.");
            }
        }
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
