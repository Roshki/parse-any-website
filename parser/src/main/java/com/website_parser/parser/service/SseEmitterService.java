package com.website_parser.parser.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    public SseEmitter createEmitter(String guid) {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> log.info("Received onCompletion request"));
        emitter.onError(emitter::completeWithError);
        emitters.put(guid, emitter);
        return emitter;
    }


    public void sendSse(String data, String guid, String topic) {
        SseEmitter sseEmitter = emitters.get(guid);
        log.info("sending");
        try {
            sseEmitter.send(SseEmitter.event().name(topic).data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void completeSse(String guid) {
        SseEmitter sseEmitter = emitters.get(guid);
        sseEmitter.complete();
        emitters.remove(guid);
    }

    public void completeSseWithError(Throwable e, String guid) {
        SseEmitter sseEmitter = emitters.get(guid);
        sseEmitter.completeWithError(e);
        emitters.remove(guid);

    }
}
