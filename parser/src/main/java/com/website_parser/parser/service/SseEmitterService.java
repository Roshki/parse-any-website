package com.website_parser.parser.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
public class SseEmitterService {
    private SseEmitter emitter;


    public SseEmitter createEmitter() {
        emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> log.info("Received onCompletion request"));
        emitter.onError((Throwable t) -> emitter.completeWithError(t));
        return emitter;
    }


    public void sendSse(String data) {

        System.out.println("sending");
        try {
            emitter.send(SseEmitter.event().name("test").data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void completeSse() {
        emitter.complete();
    }

    public void completeSseWithError(Throwable e) {

        emitter.completeWithError(e);

    }
}
