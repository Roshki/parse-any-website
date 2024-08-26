package com.website_parser.parser.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
public class Website {
    private URL websiteUrl;
    private String initialHtml;
    private List<String> pages;
}
