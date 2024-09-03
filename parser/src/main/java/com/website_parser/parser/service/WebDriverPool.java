package com.website_parser.parser.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class WebDriverPool {

    private static final int MAX_WEBDRIVERS = 3;
    private final BlockingQueue<WebDriver> driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);
    private static final String userAgent = "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";

    public void addToPool() {
        for (int i = 0; i < MAX_WEBDRIVERS; i++) {
            try {
                driverPool.add(getChromeDriver());
            } catch (IllegalStateException e) {
                System.out.println("Queue is full, release all drivers");
                releaseAllDrivers(driverPool.stream().toList());
            }
        }
    }

    public WebDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        // options.addArguments("--headless");
        options.addArguments(userAgent);
        return new ChromeDriver(options);
    }

    public WebDriver getDriverPool() {
        try {
            return driverPool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseDriver(WebDriver webDriver) {
        try {
            driverPool.put(webDriver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseAllDrivers(List<WebDriver> webDrivers) {
        webDrivers.forEach(webDriver -> System.out.println(driverPool.offer(webDriver)));
    }

    public WebDriver reconnectToBrowser(WebDriver driver) {
        System.out.println("reconnectToBrowser reconnecting....");
        if (driver != null) {
            driver.quit();
        }
        return getChromeDriver();
    }

}
