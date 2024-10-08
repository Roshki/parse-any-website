package com.website_parser.parser.service;


import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterService {
    private SseEmitter emitter;


    public SseEmitter createEmitter() {
        emitter = new SseEmitter(0L);
        return emitter;
    }

    public void sendSse(String data) {

        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void completeSse() {
        try {
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
