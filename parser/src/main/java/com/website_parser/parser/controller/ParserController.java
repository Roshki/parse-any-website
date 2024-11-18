package com.website_parser.parser.controller;

import com.website_parser.parser.components.Website;
import com.website_parser.parser.service.*;
import com.website_parser.parser.util.AddFeaturesUtil;
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
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage, @RequestParam String pageTag, @RequestParam String pageStart, @RequestParam String pageFinish, @RequestParam String userGuid) throws ExecutionException, InterruptedException {
        return paginationService.getHtmlOfAllPagesBasedOnLastPage(lastPage, pageTag, pageStart, pageFinish, userGuid, false);
    }

    @GetMapping("/approve")
    public String approve(@RequestParam String userGuid) {
        approvalService.completeApproval(userGuid);
        return "Approved!";
    }

    @PostMapping("/none-cached-page")
    public String getNotCached(@RequestBody String url, @RequestParam String userGuid) {
        System.out.println(url);
        try {
            return parserService.getNotCachedPage(url, userGuid);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/infinite-scroll")
    public ResponseEntity<String> getInfiniteScrolling(@RequestBody String url, @RequestParam String speed, @RequestParam String amount, @RequestParam String userGuid) {
        try {
            return new ResponseEntity<>(
                    scrollingService.getInfiniteScrolling(url, speed, Integer.parseInt(amount), userGuid), HttpStatus.OK);
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
        SseEmitter emitter = sseEmitterService.createEmitter("queue" + userGuid);
        if (!webDriverPoolService.ifAvailableDriver()) {
            userService.putToQueue(userGuid);
            ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

            scheduledExecutor.scheduleAtFixedRate(() ->
                    userService.notifyQueuePosition(userGuid, purpose), 2, 5, TimeUnit.SECONDS);
            emitter.onCompletion(scheduledExecutor::shutdownNow);
        } else {
            sseEmitterService.sendSse("you are next", "queue" + userGuid);
        }
        return emitter;
    }

    @PostMapping("/regex")
    public List<String> applyRegex(@RequestBody List<String> strings, @RequestParam String regex) {
        return AddFeaturesUtil.getRegex(strings, regex);

    }

}
