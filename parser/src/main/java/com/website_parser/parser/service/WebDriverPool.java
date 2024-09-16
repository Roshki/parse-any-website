package com.website_parser.parser.service;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class WebDriverPool {

    private static final Logger log = LoggerFactory.getLogger(WebDriverPool.class);
    private final BlockingQueue<WebDriver> driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);

    private static final int MAX_WEBDRIVERS = 1;
    private static final String userAgent = "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
    String remoteServerUrl = "http://localhost:4444/wd/hub";

    public void addToPool() {
        for (int i = 0; i < MAX_WEBDRIVERS; i++) {
            try {
                driverPool.add(getRemoteChromeDriver());
            } catch (IllegalStateException | MalformedURLException e) {
                System.out.println("Queue is full, release all drivers");
                releaseAllDrivers(driverPool.stream().toList());
            }
        }
    }

    private WebDriver getChromeDriver() {
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


    public WebDriver getRemoteChromeDriver() throws MalformedURLException {
        String remoteServerUrl = "http://192.168.1.25:4444";
        URL serverurl = null;
        try {
            serverurl = new URL(remoteServerUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc

//        options.addArguments("--disable-dev-shm-usage");
// options.addArguments("--headless");
        options.addArguments(userAgent);
        RemoteWebDriver driver = new RemoteWebDriver(serverurl, options, false);
        //driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        return driver;
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

    public WebDriver reconnectToBrowser(WebDriver driver) throws MalformedURLException {
        System.out.println("reconnectToBrowser reconnecting....");
        try {
            driver.quit();
        } catch (Exception e) {
            log.warn("Driver couldn't find session!");
        }

        return getRemoteChromeDriver();
    }

    @PreDestroy
    private void quitDrivers() {
        for (WebDriver webDriver : driverPool) {
            webDriver.quit();
        }
    }

}
