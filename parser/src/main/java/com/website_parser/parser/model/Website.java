package com.website_parser.parser.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Website implements Serializable {

    @Serial
    private static final long serialVersionUID = 6529685098267757690L;

    private URL websiteUrl;
    private String initialHtml;
    private boolean ifConfirmed;
    private Map<String, String> pages = Collections.emptyMap();

    public Website(){}
}
