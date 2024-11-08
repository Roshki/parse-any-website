package com.website_parser.parser.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@Slf4j
@Getter
public class ApprovalService {

    private final ConcurrentHashMap<String, CompletableFuture<Void>> pendingFutures = new ConcurrentHashMap<>();

    public String createApprovalFuture(String futureId) {
        CompletableFuture<Void> approvalFuture = new CompletableFuture<>();
        pendingFutures.put(futureId, approvalFuture);
        return futureId;
    }

    public void completeApproval(String userGuid) {
        if (pendingFutures.containsKey(userGuid)) {
            CompletableFuture<Void> approvalFuture = pendingFutures.get(userGuid);
            approvalFuture.complete(null);
        }
    }

    public void waitForApproval(long timeout, String futureId) {
        try {
            CompletableFuture<Void> future = pendingFutures.get(futureId);
            long startTime = System.nanoTime();
            log.info("tries to approve for future(userId): {}", futureId);
            future.get(timeout, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            log.info("approves future(userId): {}", futureId);
            long elapsedTime = endTime - startTime;
            double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
            log.info("Time taken: {} for future(userId): {}", elapsedTimeInSeconds, futureId);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pendingFutures.remove(futureId);
            throw new RuntimeException(e);
        }
    }

}