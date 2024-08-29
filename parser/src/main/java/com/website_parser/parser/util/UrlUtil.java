package com.website_parser.parser.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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


    public static String verifyHost(String link, URL mainUrl) {
        if (!link.contains("http")) {
            return mainUrl.getProtocol() + "://" + mainUrl.getHost() + link;
        }
        return link;
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
