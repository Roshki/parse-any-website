package com.website_parser.parser.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    public static ArrayList<String> predictAllUrls(String lastUrl) {
        ArrayList<String> urlList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)(?!.*\\d)");
        Matcher matcher = pattern.matcher(lastUrl);
        if (matcher.find()) {
            String currentNumber = matcher.group(1);
            for (int i = Integer.parseInt(currentNumber)+1; i > 1; i--) {
                String nextNumber = String.valueOf(i - 1);
                urlList.add(matcher.replaceFirst(nextNumber));
            }
            return urlList;
        }
        return null;
    }
}
