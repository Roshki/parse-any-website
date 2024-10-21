package com.website_parser.parser.controller;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"${frontend.url}"})
public class ParserController {
    private final ParserService parserService;
    private final ApprovalService approvalService;
    private final PaginationService paginationService;
    private final ScrollingService scrollingService;
    private final SseEmitterService sseEmitterService;

    @PostMapping("/send-html")
    public String getHtml(@RequestBody String url) {
        System.out.println("cached??");
        return parserService.getCachedPage(url);
    }

    @PostMapping("/last-page")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage, @RequestParam String pageTag, @RequestParam String pageStart, @RequestParam String pageFinish, @RequestParam String userGuid) throws ExecutionException, InterruptedException, MalformedURLException {
        List<String> pages = paginationService.getHtmlOfAllPagesBasedOnLastPage(lastPage, pageTag, pageStart, pageFinish, userGuid);
        sseEmitterService.completeSse(userGuid);
        return pages;
    }

    @GetMapping("/approve")
    public String approve(@RequestParam String userGuid) {
        approvalService.approve(userGuid);
        return "Approved!";
    }

    @PostMapping("/none-cached-page")
    public String getNotCached(@RequestBody String url, @RequestParam String userGuid) throws Exception {
        System.out.println(url);
        return parserService.getNotCachedPage(url, userGuid);
    }

    @PostMapping("/infinite-scroll")
    public ResponseEntity<String> getInfiniteScrolling(@RequestBody String url, @RequestParam String speed, @RequestParam String userGuid) {
        try {
            return new ResponseEntity<>(
                    scrollingService.getInfiniteScrolling(url, speed, 100, userGuid), HttpStatus.OK);
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("error occurred! " + e, HttpStatusCode.valueOf(500));
        }
    }

    @PostMapping("/html-page-cleanup")
    public String getCleanHtml(@RequestBody Website website) throws MalformedURLException {
        // approvalService.approve();
        return parserService.getCleanHtml(website);
    }

    @GetMapping("/connect")
    public String test() {
        return parserService.ifWebDriverConn();
    }

    @GetMapping("/sse")
    public SseEmitter streamSseMvc(@RequestParam String userGuid) {
        SseEmitter emitter = sseEmitterService.createEmitter(userGuid);
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("{\"status\": \"alive\"}"));
            } catch (IOException e) {
                emitter.completeWithError(e);
                scheduledExecutor.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);
        return emitter;
    }
}
