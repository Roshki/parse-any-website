package com.website_parser.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class WebDriverService {

    private static final int MAX_WEBDRIVERS = 3;
    private final BlockingQueue<WebDriver> driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);
    private WebDriver initialDriver;
    private static final String userAgent = "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";

    @Value("${parser.remote-chrome-1}")
    private String chromePort1;
    @Value("${parser.remote-chrome-2}")
    private String chromePort2;
    @Value("${parser.remote-chrome-3}")
    private String chromePort3;

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

    public WebDriver getRemoteChromeDriver(String chromePort) {

        //String remoteServerUrl = "http://192.168.1.25:4444";
        //System.out.println("CHROMEPORT is:  " + chromePort);
        // String remoteServerUrl = "https://" + chromePort + ":4444";
        URL serverurl = null;
        try {
            serverurl = new URL(chromePort);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("--mute-audio");
        options.addArguments("--incognito");
        options.addArguments(userAgent);
        return new RemoteWebDriver(serverurl, options, false);
    }


    public void addToPool() {
        ArrayList<String> chromesList = new ArrayList<>(MAX_WEBDRIVERS);
        Collections.addAll(chromesList, chromePort1, chromePort3, chromePort2);
        if (driverPool.size() < MAX_WEBDRIVERS) {
            for (int i = 0; i < MAX_WEBDRIVERS; i++) {
                try {
                    driverPool.add(getRemoteChromeDriver(chromesList.get(i)));
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
            initialDriver = getRemoteChromeDriver(chromePort1);
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
//        try {
//            driver.close();
//        } catch (Exception ex) {
//            log.warn("driver is already closed");
//        }
//        try {
//            driver.quit();
//        } catch (Exception ex) {
//            log.warn("it didn't find the session to quit");
//        }
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
