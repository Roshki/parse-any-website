package com.website_parser.parser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebDriverService {

    private static final int MAX_WEBDRIVERS = 3;
    private final BlockingQueue<WebDriver> driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);
    private WebDriver initialDriver;
    private final ApplicationContext applicationContext;


    public void initPool() {
        if (driverPool.size() < MAX_WEBDRIVERS) {
            for (int i = 0; i < MAX_WEBDRIVERS; i++) {
                try {
                    WebDriver newDriver = applicationContext.getBean(WebDriver.class);
                    driverPool.add(newDriver);
                } catch (IllegalStateException e) {
                    log.warn("driver pool is full");
                }
            }
        }
    }

    public void validateAndRecreateDrivers() {
        List<WebDriver> invalidDrivers = new ArrayList<>();
        for (WebDriver driver : driverPool) {
            if (isDriverInvalid(driver)) {
                invalidDrivers.add(driver);
            }
        }
        for (WebDriver invalidDriver : invalidDrivers) {
            driverPool.remove(invalidDriver);
            driverPool.add(recreateWebDriver(invalidDriver));
        }
    }

    public WebDriver verifyAndGetWebDriver(WebDriver driver) {
        try {
            if (driver == null || isDriverInvalid(driver)) {
                return recreateWebDriver(driver);
            }
        } catch (Exception e) {
            return recreateWebDriver(driver);
        }
        return driver;
    }

    private boolean isDriverInvalid(WebDriver driver) {
        try {
            driver.getTitle();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private WebDriver recreateWebDriver(WebDriver driver) {
        if (driver != null) {
            try {
                safelyCloseAndQuitDriver(driver);
            } catch (Exception e) {
                log.error("error occurred {}", e, e);
            }
        }
        return applicationContext.getBean(WebDriver.class);
    }

    public void closeInitialDriver() {
        safelyCloseAndQuitDriver(initialDriver);
    }

    public WebDriver verifyInitialDriver() {
        initialDriver = verifyAndGetWebDriver(initialDriver);
        return initialDriver;
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
            validateAndRecreateDrivers();
            return driverPool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseDriverToThePool(WebDriver webDriver) {
        try {
            validateAndRecreateDrivers();
            driverPool.put(webDriver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
