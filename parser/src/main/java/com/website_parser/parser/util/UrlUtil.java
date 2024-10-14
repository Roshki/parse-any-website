package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UrlUtil {

    public static ArrayList<String> predictAllUrls(String lastUrl) {
        ArrayList<String> urlList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)(?!.*\\d)");
        Matcher matcher = pattern.matcher(lastUrl);
        if (matcher.find()) {
            String currentNumber = matcher.group(1);
            for (int i = Integer.parseInt(currentNumber) + 1; i > 1; i--) {
                String nextNumber = String.valueOf(i - 1);
                urlList.add(matcher.replaceFirst(nextNumber));
            }
            return urlList;
        }
        return null;
    }

    public static List<String> predictAllUrls(String url, String tagName, String startPage, String endPage) {
        List<String> updatedUrls = new ArrayList<>();
        String regex = tagName + "=\\d+";  //page=1
        Pattern pattern = Pattern.compile(regex);
        for (int i = Integer.parseInt(startPage); i <= Integer.parseInt(endPage); i++) {
            Matcher matcher = pattern.matcher(url);
            String updatedUrl = matcher.replaceAll(tagName + "=" + i);
            updatedUrls.add(updatedUrl);
        }
        return updatedUrls;
    }

    public static String verifyHost(String link, URL mainUrl) {

        return switch (getUrlType(link)) {
            case ABSOLUTE, ABSOLUTE_WITHOUT_WWW -> link;
            case RELATIVE_WITHOUT_PROTOCOL -> mainUrl.getProtocol() + "://" + mainUrl.getHost() + link;
            case PROTOCOL_RELATIVE_WWW -> mainUrl.getProtocol() + ":" + link;
            case RELATIVE_WWW -> mainUrl.getProtocol() + "://" + link;
        };
    }

    private static UrlType getUrlType(String link) {
        if (link.contains("http")) {
            return link.contains("www.") ? UrlType.ABSOLUTE : UrlType.ABSOLUTE_WITHOUT_WWW;
        }
        if (link.contains("www.")) {
            return link.contains("//www.") ? UrlType.PROTOCOL_RELATIVE_WWW : UrlType.RELATIVE_WWW;
        } else {
            return UrlType.RELATIVE_WITHOUT_PROTOCOL;
        }
    }

    private enum UrlType {
        ABSOLUTE,                   // Contains http and www.
        ABSOLUTE_WITHOUT_WWW,        // Contains http but no www.
        PROTOCOL_RELATIVE_WWW,       // Starts with //www.
        RELATIVE_WWW,                // Contains www. but no //.
        RELATIVE_WITHOUT_PROTOCOL    // No protocol and no www.
    }

    public static String getContent(String anyUrl) throws Exception {
        URL url = new URL(anyUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }
        in.close();
        connection.disconnect();

        return content.toString();
    }

}
