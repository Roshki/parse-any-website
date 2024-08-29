package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

import static com.website_parser.parser.util.UrlUtil.getContent;
import static com.website_parser.parser.util.UrlUtil.verifyHost;

@Slf4j
public class CssUtil {

    public static String cssLinkToStyle(String htmlContent, URL mainUrl) {
        Document doc = Jsoup.parse(htmlContent);
        Elements linkElements = doc.select("link[rel=stylesheet]");
        linkElements.addAll(doc.select("link[rel=preload]"));
        for (Element linkElement : linkElements) {
            String cssUrl = linkElement.attr("href");
            String verifiedHost = verifyHost(cssUrl, mainUrl);
            String cssContent = null;
            try {
                cssContent = getContent(verifiedHost);
            } catch (Exception e) {
                log.warn("not possible to fetch css + {}", verifiedHost);
            }
            Element styleElement = doc.createElement("style");
            if (cssContent != null) {
                styleElement.appendText(cssContent);
                linkElement.replaceWith(styleElement);
                System.out.println("Substituted CSS link with inline style: " + styleElement);
            }
            htmlContent = doc.html();
        }

        return htmlContent;
    }

}
