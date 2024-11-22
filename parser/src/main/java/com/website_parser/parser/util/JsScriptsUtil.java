package com.website_parser.parser.util;


import java.util.Arrays;
import java.util.List;

public class JsScriptsUtil {

    public static final String returnPageHeightScript = "return document.body.scrollHeight";


    public static String scrollDown(String speed) {
        return "window.scrollTo(0, document.body.scrollHeight-" + speed + ");";
    }

    public static String getBannerCssSelectors() {
        return "*[class*='modal'], " +
                "*[class*='popup'], " +
                "*[class*='alert'], " +
                "*[class*='message']";
    }

    public static String getIframePath() {
        return "//div//iframe";
    }

    public static List<String> getBannersButtonSelectors() {
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

}
