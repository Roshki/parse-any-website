package com.website_parser.parser.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Getter
@Service
public class ApprovalService {

    private CompletableFuture<Void> approvalFuture = new CompletableFuture<>();

    public void approve() {
        approvalFuture.complete(null);
    }

    public void reset() {
        approvalFuture = new CompletableFuture<>();
    }
}