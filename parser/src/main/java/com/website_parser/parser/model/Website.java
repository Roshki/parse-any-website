package com.website_parser.parser.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Website implements Serializable {

    @Serial
    private static final long serialVersionUID = 6529685098267757690L;

    private String websiteUrl;
    private String initialHtml;
    private Map<String, String> pages;

    public Website(String websiteUrl, String initialHtml, Map<String, String> pages) {
        this.websiteUrl = websiteUrl;
        this.initialHtml = initialHtml;
        this.pages = pages;
    }

    public void populateWebsite(Website retrievedWebsite) {
        this.setPages(retrievedWebsite.getPages());
        this.setWebsiteUrl(retrievedWebsite.getWebsiteUrl());
        this.setInitialHtml(retrievedWebsite.getInitialHtml());
    }

}
