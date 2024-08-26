package com.website_parser.parser.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

@Component
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Website implements Serializable {

    private URL websiteUrl;
    private String initialHtml;
    private Map<String, String> pages;

    public Website(){}
}
