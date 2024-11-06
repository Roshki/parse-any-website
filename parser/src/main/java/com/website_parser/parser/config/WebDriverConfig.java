package com.website_parser.parser.config;

import com.website_parser.parser.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

@Configuration
@Slf4j
public class WebDriverConfig {

    private static final String userAgent = "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
    private static final String userProfile = "";

    @Bean
    @Profile("dev")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebDriver getChromeDriver() {
        System.out.println("hello from local");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        //options.addArguments("--headless=new");
        options.addArguments("--enable-gpu");
//        options.addArguments("user-data-dir=" + userProfile);
//        options.addArguments("profile-directory=Default");
        //setProxy(options);
        options.addArguments(userAgent);
        return new ChromeDriver(options);
    }

    @Bean
    @Profile("prod")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebDriver getRemoteChromeDriver(@Value("${parser.remote-chrome-1}") String chromePort1, @Value("${parser.remote-chrome-2}") String chromePort2, @Value("${parser.remote-chrome-3}") String chromePort3) throws MalformedURLException {
        System.out.println("hello from remote");
        ArrayList<String> chromesList = new ArrayList<>();
        Collections.addAll(chromesList, chromePort3, chromePort2);
        URL serverurl = new URL(chromePort1);
        System.out.println(serverurl + " serverurl");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("--mute-audio");
        options.addArguments("--incognito");
        options.addArguments(userAgent);
        //setProxy(options);
        try {
            return createRemoteWebDriver(serverurl, options);
        } catch (Exception ex) {
            return tryFallbackRemoteWebDriver(chromesList, serverurl, options);
        }
    }

    private static void setProxy(ChromeOptions options) {
        Proxy proxy = ProxyUtil.getProxy();
        if (proxy != null) {
            options.setProxy(proxy);
            options.setCapability("proxy", proxy);
        }
    }

    private WebDriver createRemoteWebDriver(URL url, ChromeOptions options) throws TimeoutException {
        RemoteWebDriver driver = new RemoteWebDriver(url, options, false);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(260));
        if (driver.getSessionId() != null) {
            log.info("Connected to Remote WebDriver successfully. Session ID: {}", driver.getSessionId());
        } else {
            log.error("Connection failed. No session created.");
        }
        return driver;
    }

    private WebDriver tryFallbackRemoteWebDriver(List<String> chromePorts, URL primaryServerUrl, ChromeOptions options) {
        for (String server : chromePorts) {
            try {
                if (!server.equals(primaryServerUrl.toString())) {
                    URL fallbackUrl = new URL(server);
                    log.info("Attempting to connect to fallback WebDriver server: {}", fallbackUrl);
                    return createRemoteWebDriver(fallbackUrl, options);
                }
            } catch (MalformedURLException e) {
                log.warn("Invalid fallback WebDriver URL: {}", server);
            } catch (Exception ex) {
                log.warn("Fallback WebDriver failed: {}", ex.getMessage());
            }
        }
        log.error("Both primary and fallback WebDriver instances failed");
        throw new RuntimeException("Could not connect to any WebDriver instance");
    }

}
