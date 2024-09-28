package com.website_parser.parser.controller;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ParserController {
    private final ParserService parserService;
    private final SavingService savingService;
    private final ApprovalService approvalService;
    private final PaginationService paginationService;
    private final ScrollingService scrollingService;

    @PostMapping("/send-html")
    public String getHtml(@RequestBody String url) {
        System.out.println("cached??");
        return parserService.getCachedPage(url);
    }

    @PostMapping("/cached-page")
    public String getCached(@RequestBody String url) {
        return parserService.getCachedPage(url);
    }

    @PostMapping("/last-page")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage) throws ExecutionException, InterruptedException, MalformedURLException {
        return paginationService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
    }

    @PostMapping("/get-info-url")
    public String getAllPagesBasedOnLastPage(@RequestBody Map<String, List<String>> map) {
        System.out.println(map.size());
        //map.forEach((key, value) -> System.out.println(key + " -> " + value));
        System.out.println(Paths.get(System.getProperty("user.home"), "Desktop"));
        savingService.exportMapToExcel(map, Paths.get(System.getProperty("user.home"), "Desktop") + "/data.xlsx");
        return "success";
    }

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

    @PostMapping("/infinite-scroll")
    public ResponseEntity<String> getInfiniteScrolling(@RequestBody String url, @RequestParam String speed) {
        try {
            return new ResponseEntity<>(
                    scrollingService.getInfiniteScrolling(url, speed), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("error occurred! " + ex, HttpStatusCode.valueOf(500));
        }
    }

    @PostMapping("/html-page-cleanup")
    public String getNotCached(@RequestBody Website website) throws MalformedURLException {
        // approvalService.approve();
        System.out.println(website);
        return parserService.getCleanHtml(website);
    }

    @GetMapping("/connect")
    public String test() {
        return parserService.ifWebDriverConn();
    }
}
