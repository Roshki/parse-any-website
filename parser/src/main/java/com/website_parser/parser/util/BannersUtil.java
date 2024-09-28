package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class BannersUtil {

    public static List<String> getButtonSelectors() {
        return Arrays.asList(
                "//button[text()= 'Dismiss']",
                "//button[text()= 'Close']",
                "//button[text()= 'Accept']",
                "//button[contains(text(), 'Agree')]",
                "//button[text()= 'Accepteren']",
                "button[title='Accepteren']",
                "button[class*='dismiss']",
                ".cookie-banner button",
                ".consent-banner button",
                "button[aria-label='Close']"
        );
    }
    public static void handleBannerIfPresent(WebDriver driver) {
        if (isBannerPresent(driver)) {
            log.info("Banner is present.");
            boolean ifProcessed = ifProcessedBannerButtons(driver);
            if (!ifProcessed) {
                log.info("No buttons to press found. Attempting to switch to iframe.");
                switchToBannerIframeIfPresent(driver);
                ifProcessed = ifProcessedBannerButtons(driver);
                System.out.println(ifProcessed);
            }
        }
        driver.switchTo().defaultContent();
    }


    private static boolean ifProcessedBannerButtons(WebDriver driver) {
        List<WebElement> buttons = new ArrayList<>();
        for (String selector : getButtonSelectors()) {
            if (selector.startsWith("//")) {
                buttons.addAll(driver.findElements(By.xpath(selector)));
            } else {
                buttons.addAll(driver.findElements(By.cssSelector(selector)));
            }
        }
        if (!buttons.isEmpty()) {
            for (WebElement button : buttons) {
                if (button.isDisplayed()) {
                    button.click();
                    log.info("Clicked on consent button");
                    return true;
                }
            }
            log.info("No visible buttons found on the banner.");
        } else {
            log.info("No buttons found on the banner.");
        }
        return false;
    }


    private static boolean isBannerPresent(WebDriver driver) {
        try {
            WebElement modal = driver.findElement(By.cssSelector("*[class*='modal'], *[class*='popup'], *[class*='alert'], *[class*='message']"));
            return modal.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private static void switchToBannerIframeIfPresent(WebDriver driver) {
        try {
            WebElement iframeElement = driver.findElement(By.xpath("//div//iframe"));
            if (iframeElement != null) {
                driver.switchTo().frame(iframeElement);
                log.info("Switched to banner iframe.");
            }
        } catch (NoSuchElementException e) {
            log.info("the banner is not in iframe");
        }
    }

}
