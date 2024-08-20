package com.website_parser.parser.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PaginationService {

    public String getLastPage(List<WebElement> pagesWithGap) {
        Set<String> hrefPagesWithGap = new LinkedHashSet<>();
        pagesWithGap.forEach(p -> hrefPagesWithGap.add(p.getAttribute("href")));
        hrefPagesWithGap.forEach(System.out::println);
        String lastElement = null;
        for (String href : hrefPagesWithGap) {
            lastElement = href;
        }
        return lastElement;
    }


    public List<WebElement> getPagination(String paginationTag, WebDriver driver) {
        return driver.findElements(By.xpath("//div[contains(@class, '" + paginationTag + "')][1]//a"));
    }

}
