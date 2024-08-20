package com.website_parser.parser.controller;

import com.website_parser.parser.service.ParserService;
import com.website_parser.parser.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


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
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage){
        return parserService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
    }

    @GetMapping("test")
    public ArrayList<String> diffTest(){
      //  return StringUtil.getUrlDifference("https://www.goodreads.com/list/show/3810.Best_Cozy_Mystery_Series?page=1", "https://www.goodreads.com/list/show/3810.Best_Cozy_Mystery_Series?page=10");
        return UrlUtil.predictAllUrls("https://example.com/list?category=books&page=20&sort=asc");
    }
}
