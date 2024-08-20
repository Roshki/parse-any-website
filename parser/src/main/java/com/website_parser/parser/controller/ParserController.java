package com.website_parser.parser.controller;

import com.website_parser.parser.service.ParserService;
import com.website_parser.parser.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


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
}
