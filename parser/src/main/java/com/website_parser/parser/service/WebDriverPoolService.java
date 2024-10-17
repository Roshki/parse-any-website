package com.website_parser.parser.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class WebDriverPoolService extends WebDriverService {

    @Value("${parser.threads-amnt:1}")
    private int MAX_WEBDRIVERS;

    private BlockingQueue<WebDriver> driverPool;

    public WebDriverPoolService(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @PostConstruct
    public void initPool() {
        driverPool = new LinkedBlockingQueue<>(MAX_WEBDRIVERS);
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

    @PreDestroy
    public void destroyPool() {
        driverPool.forEach(this::safelyCloseAndQuitDriver);
    }


public void validateAndRecreateDrivers() {
    List<WebDriver> invalidDrivers = new ArrayList<>();
    for (WebDriver driver : driverPool) {
        if (!isDriverValid(driver)) {
            invalidDrivers.add(driver);
        }
    }
    for (WebDriver invalidDriver : invalidDrivers) {
        driverPool.remove(invalidDriver);
        safelyCloseAndQuitDriver(invalidDriver);
        System.out.println("removed invalid driver");
        driverPool.add(createAndReturnWebDriver());
    }
}

public WebDriver getDriverFromPool() {
    validateAndRecreateDrivers();
    try {
        return driverPool.take();
    } catch (InterruptedException e) {
        throw new RuntimeException("Error retrieving WebDriver from pool", e);
    }
}

public void releaseDriverToThePool(WebDriver webDriver) {
    validateAndRecreateDrivers();
    try {
        driverPool.put(webDriver);
    } catch (InterruptedException e) {
        throw new RuntimeException("Error releasing WebDriver to pool", e);
    }
}
}
