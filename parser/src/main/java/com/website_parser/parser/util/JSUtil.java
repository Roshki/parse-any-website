package com.website_parser.parser.util;

import org.openqa.selenium.JavascriptExecutor;

public class JSUtil {

    private static final String returnPageHeightScript = "return document.body.scrollHeight";

    public static void scrollDown(String speed, JavascriptExecutor jsExecutor) {
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight-" + speed + ");");
    }



}
