package com.website_parser.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class WebDriverService {

    private static final int MAX_WEBDRIVERS = 3;
    private final BlockingQueue<WebDriver> driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);
    private WebDriver initialDriver;
    private static final String userAgent = "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";

    public WebDriver getNewChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--headless");
        //options.setProxy(ProxyUtil.getRandomProxy());
        //options.setCapability("proxy", ProxyUtil.getRandomProxy());
        options.addArguments(userAgent);
        return new ChromeDriver(options);
    }


    public void addToPool() {
        if (driverPool.size() < MAX_WEBDRIVERS) {
            for (int i = 0; i < MAX_WEBDRIVERS; i++) {
                try {
                    driverPool.add(getNewChromeDriver());
                } catch (IllegalStateException e) {
                    log.warn("driver pool is full");
                }
            }
        }
    }

    public void closeAllPool() {
        for (int i = 0; i < driverPool.size(); i++) {
            driverPool.iterator().forEachRemaining(this::safelyCloseAndQuitDriver);
        }
        driverPool.clear();
    }

    public WebDriver getInitialDriver() {
        if (initialDriver == null) {
            initialDriver = getNewChromeDriver();
        }
        return initialDriver;
    }

    public void closeInitialDriver() {
        if (initialDriver != null) {
            safelyCloseAndQuitDriver(initialDriver);
            initialDriver = null;
        }
    }

    public void safelyCloseAndQuitDriver(WebDriver driver) {
        try {
            driver.close();
        } catch (Exception ex) {
            log.warn("driver is already closed");
        }
        try {
            driver.quit();
        } catch (Exception ex) {
            log.warn("it didn't find the session to quit");
        }
    }

    public WebDriver getDriverFromPool() {
        try {
            return driverPool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseDriverToThePool(WebDriver webDriver) {
        try {
            driverPool.put(webDriver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
