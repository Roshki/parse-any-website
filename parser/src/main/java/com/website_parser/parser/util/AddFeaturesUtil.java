package com.website_parser.parser.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFeaturesUtil {

    public static List<String> getRegex(List<String> entries, String reg) {
        List<String> updatedUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile(reg);
        for (String entry : entries) {
            Matcher matcher = pattern.matcher(entry);
            StringBuilder updatedUrl = new StringBuilder();
            while (matcher.find()) {
                updatedUrl.append(matcher.group()).append(" ");
            }
            updatedUrls.add(updatedUrl.toString().trim());
        }
        return updatedUrls;
    }
}
