package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BannersUtil {

    private static boolean isBannerPresent(WebDriver driver) {
        try {
            WebElement modal = driver.findElement(By.cssSelector("*[class*='modal'], *[class*='popup'], *[class*='alert'], *[class*='message']"));
            return modal.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void handleBannerIfPresent(WebDriver driver) {
        boolean isPresent = isBannerPresent(driver);
        List<WebElement> buttons = new ArrayList<>();
        System.out.println("???-" + isPresent + "-???");
        if (isPresent) {
            try {
                String[] buttonSelectors = {
                        "//button[text()= 'Dismiss']",
                        "//button[text()= 'Close']",
                        "//button[text()= 'Accept']",
                        "//button[contains(text(), 'Agree')]",
                        "//button[text()= 'Accepteren']",
                        "button[title='Accepteren']",
                        "button[class*='dismiss']",
                        ".cookie-banner button",
                        ".consent-banner button"
                };
                for (String selector : buttonSelectors) {
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
                            System.out.println("Clicked on cookies consent button: " + button.getText());
                            return;
                        }
                        System.out.println(button.getText() + "not displayed");
                    }
                }
                System.out.println("No cookies consent pop-up found.");
            } catch (Exception e) {
                System.out.println("Error handling banner: " + e.getMessage());
            }
        }
    }
}
