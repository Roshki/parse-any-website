package com.website_parser.parser.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Component
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Website implements Serializable {

    @Serial
    private static final long serialVersionUID = 6529685098267757690L;

    private String websiteUrl;
    private String initialHtml;
    private Map<String, String> pages;
    private boolean withRegistration = false;

    public void populateWebsite(Website retrievedWebsite) {
        this.setPages(retrievedWebsite.getPages());
        this.setWebsiteUrl(retrievedWebsite.getWebsiteUrl());
        this.setInitialHtml(retrievedWebsite.getInitialHtml());
    }
}
