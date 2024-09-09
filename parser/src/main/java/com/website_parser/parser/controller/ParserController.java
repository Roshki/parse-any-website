package com.website_parser.parser.controller;

import com.website_parser.parser.service.ApprovalService;
import com.website_parser.parser.service.ParserService;
import com.website_parser.parser.service.SavingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ParserController {
    private final ParserService parserService;
    private final SavingService savingService;
    private final ApprovalService approvalService;

    @PostMapping("/send-html")
    public String getHtml(@RequestBody String url) throws Exception {
        System.out.println("cached??");
        return parserService.getCachedPage(url);
    }

    @PostMapping("/cached-page")
    public String getCached(@RequestBody String url) throws Exception {
        return parserService.getCachedPage(url);
    }

    @PostMapping("/last-page")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage) throws ExecutionException, InterruptedException {
        return parserService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
    }

    @PostMapping("/get-info-url")
    public String getAllPagesBasedOnLastPage(@RequestBody Map<String, List<String>> map) {
        System.out.println(map.size());
        // Print the map
        //map.forEach((key, value) -> System.out.println(key + " -> " + value));
        savingService.exportMapToExcel(map, "data_books.xlsx");
        return "success";
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/approve")
    public String approve() {
        approvalService.approve();
        return "Approved!";
    }

    @PostMapping("/none-cached-page")
    public String getNotCached(@RequestBody String url) throws Exception {
        System.out.println(url);
        return parserService.getNotCachedPage(url);
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
