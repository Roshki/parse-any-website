package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.website_parser.parser.util.JsScriptsUtil.*;

@Slf4j
public class BannersUtil {

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
        for (String selector : getBannersButtonSelectors()) {
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
            WebElement modal = driver.findElement(By.cssSelector(getBannerCssSelectors()));
            return modal.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private static void switchToBannerIframeIfPresent(WebDriver driver) {
        try {
            WebElement iframeElement = driver.findElement(By.xpath(getIframePath()));
            if (iframeElement != null) {
                driver.switchTo().frame(iframeElement);
                log.info("Switched to banner iframe.");
            }
        } catch (NoSuchElementException e) {
            log.info("the banner is not in iframe");
        }
    }

}
