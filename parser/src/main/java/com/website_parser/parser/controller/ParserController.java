package com.website_parser.parser.controller;

import com.website_parser.parser.model.Website;
import com.website_parser.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.*;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"${frontend.url}"})
public class ParserController {
    private final ParserService parserService;
    private final ApprovalService approvalService;
    private final PaginationService paginationService;
    private final ScrollingService scrollingService;
    private final SseEmitterService sseEmitterService;
    private final UserService userService;
    private final WebDriverPoolService webDriverPoolService;

    @PostMapping("/send-html")
    public String getHtml(@RequestBody String url) {
        System.out.println("cached??");
        return parserService.getCachedPage(url);
    }

    @PostMapping("/last-page")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage, @RequestParam String pageTag, @RequestParam String pageStart, @RequestParam String pageFinish, @RequestParam String userGuid) throws ExecutionException, InterruptedException, MalformedURLException {
        //userService.putToQueue(userGuid);
        // userService.waitForQueuePositionToBeFirst(userGuid);
        return paginationService.getHtmlOfAllPagesBasedOnLastPage(lastPage, pageTag, pageStart, pageFinish, userGuid);
    }

    @GetMapping("/approve")
    public String approve(@RequestParam String userGuid) {
        approvalService.approve(userGuid);
        return "Approved!";
    }

    @PostMapping("/none-cached-page")
    public String getNotCached(@RequestBody String url, @RequestParam String userGuid) {
        System.out.println(url);
        //   userService.putToQueue(userGuid);
        try {
            return parserService.getNotCachedPage(url, userGuid);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/infinite-scroll")
    public ResponseEntity<String> getInfiniteScrolling(@RequestBody String url, @RequestParam String speed, @RequestParam String userGuid) {
        try {
            // userService.putToQueue(userGuid);
            userService.waitForQueuePositionToBeFirst(userGuid);
            return new ResponseEntity<>(
                    scrollingService.getInfiniteScrolling(url, speed, 20, userGuid), HttpStatus.OK);
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("error occurred! " + e, HttpStatusCode.valueOf(500));
        }
    }

    @PostMapping("/html-page-cleanup")
    public String getCleanHtml(@RequestBody Website website) throws MalformedURLException {
        return parserService.getCleanHtml(website);
    }

    @GetMapping("/connect")
    public String test() {
        return parserService.ifWebDriverConn();
    }

    @GetMapping("/sse")
    public SseEmitter streamSseMvc(@RequestParam String userGuid) {
        return sseEmitterService.createEmitter("progress" + userGuid);
    }

    @GetMapping("/sse/queue")
    public SseEmitter streamQueueSse(@RequestParam String userGuid, @RequestParam String purpose) {
        userService.putToQueue(userGuid);
        SseEmitter emitter = sseEmitterService.createEmitter("queue" + userGuid);
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

        scheduledExecutor.scheduleAtFixedRate(() ->
                userService.notifyQueuePosition(userGuid, purpose), 2, 5, TimeUnit.SECONDS);
        emitter.onCompletion(scheduledExecutor::shutdownNow);
        return emitter;
    }

}
