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
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage) throws ExecutionException, InterruptedException, MalformedURLException {
        List<String> pages= paginationService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
        sseEmitterService.completeSse();
        return pages;
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
                    scrollingService.getInfiniteScrolling(url, speed, 5), HttpStatus.OK);
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("error occurred! " + e, HttpStatusCode.valueOf(500));
        }
    }

    @PostMapping("/html-page-cleanup")
    public String getNotCached(@RequestBody Website website) throws MalformedURLException {
        // approvalService.approve();
        return parserService.getCleanHtml(website);
    }

    @GetMapping("/connect")
    public String test() {
        return parserService.ifWebDriverConn();
    }

    @GetMapping("/sse")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter=sseEmitterService.createEmitter();
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        // Schedule a task to send heartbeat messages every 2 seconds
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                // Send heartbeat event
                emitter.send(SseEmitter.event().name("heartbeat").data("{\"status\": \"alive\"}"));
            } catch (IOException e) {
                // If sending the event fails, complete the emitter
                emitter.completeWithError(e);
                scheduledExecutor.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);  // Send first heartbeat immediately, then every 2 seconds
      return emitter;
    }
}
