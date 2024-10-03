package com.website_parser.parser.config;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@Configuration
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
        //options.setProxy(ProxyUtil.getRandomProxy());
        // options.setCapability("proxy", ProxyUtil.getRandomProxy());
        options.addArguments(userAgent);
        return new ChromeDriver(options);
    }

    @Bean
    @Profile("prod")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebDriver getRemoteChromeDriver(@Value("${parser.remote-chrome-1}") String chromePort1, @Value("${parser.remote-chrome-2}") String chromePort2, @Value("${parser.remote-chrome-3}") String chromePort3) {
        System.out.println("hello from remote");
        ArrayList<String> chromesList = new ArrayList<>();
        Collections.addAll(chromesList, chromePort1, chromePort3, chromePort2);

        URL serverurl;
        try {
            serverurl = new URL(chromesList.get(new Random().nextInt(chromesList.size())));
            System.out.println(serverurl + " serverurl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless=new");
        options.addArguments("--disable-extensions");
        options.addArguments("--mute-audio");
        options.addArguments("--incognito");
        options.addArguments(userAgent);
        return new RemoteWebDriver(serverurl, options, false);
    }

}
