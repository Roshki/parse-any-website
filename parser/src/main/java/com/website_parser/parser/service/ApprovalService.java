package com.website_parser.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@Slf4j
public class ApprovalService {

    private final ConcurrentHashMap<String, CompletableFuture<Void>> pendingFutures = new ConcurrentHashMap<>();

    private String createApprovalFuture(String futureId) {
        CompletableFuture<Void> approvalFuture = new CompletableFuture<>();
        pendingFutures.put(futureId, approvalFuture);
        return futureId;
    }

    public void approve(String userGuid) {
        CompletableFuture<Void> approvalFuture = pendingFutures.get(userGuid);
        approvalFuture.complete(null);
    }

    public void approveOrTimeout(long timeout, String userGuid) {
        String futureId = createApprovalFuture(userGuid);
        try {
            CompletableFuture<Void> future = pendingFutures.get(futureId);
            long startTime = System.nanoTime();
            log.info("tries to approve for user: {}", userGuid);
            future.get(timeout, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            log.info("approves user: {}", userGuid);
            long elapsedTime = endTime - startTime;
            double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
            log.info("Time taken: {} for user: {}", elapsedTimeInSeconds, userGuid);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pendingFutures.remove(futureId);
            throw new RuntimeException(e);
        }
    }

}