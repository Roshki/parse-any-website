package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlContentUtil {

    public static String updateHtmlAndReturn(String htmlContent) {;
        return removeTagsAndReturn(htmlContent);
    }


    public static String removeTagsAndReturn(String htmlContent) {
        return htmlContent
                .replaceAll("(?s)<header[^>]*>.*?</header>", "")
                .replaceAll("(?s)<nav[^>]*>.*?</nav>", "")
                .replaceAll("(?s)position: sticky;", "");
    }

}
