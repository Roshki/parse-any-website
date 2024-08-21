package com.website_parser.parser.controller;

import com.website_parser.parser.service.ParserService;
import com.website_parser.parser.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;


@RestController
@RequiredArgsConstructor
public class ParserController {
    private final ParserService parserService;

    @PostMapping("/send-html")
    @CrossOrigin(origins = "http://localhost:4200")
    public String getHtml(@RequestBody String url) throws MalformedURLException {
        System.out.println("testtest");
        return parserService.getInitialHtmlFromUrl(url);
    }

    @PostMapping("/pagination-tag")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<String> getAllPages(@RequestBody String paginationTag){
        return parserService.getHtmlOfAllPages(paginationTag);
    }

    @PostMapping("/last-page")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage) throws ExecutionException, InterruptedException {
        return parserService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
    }

    @PostMapping("/get-info-url")
    @CrossOrigin(origins = "http://localhost:4200")
    public String getAllPagesBasedOnLastPage(@RequestBody Map<String, List<String>> map)  {
        System.out.println(map.size());
        // Print the map
        map.forEach((key, value) -> System.out.println(key + " -> " + value));
        return "success";
    }
}
