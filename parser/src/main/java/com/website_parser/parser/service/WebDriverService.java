package com.website_parser.parser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class WebDriverService {

    protected final ApplicationContext applicationContext;

    public WebDriver verifyAndGetWebDriver(WebDriver driver) {
        if (driver == null || isDriverInvalid(driver)) {
            return createAndReturnWebDriver();
        }
        return driver;
    }

    protected boolean isDriverInvalid(WebDriver driver) {
        try {
            driver.getTitle();
            return false;
        } catch (Exception e) {
            log.warn("Driver is invalid");
            return true;
        }
    }

    public WebDriver createAndReturnWebDriver() {
        return applicationContext.getBean(WebDriver.class);
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

}