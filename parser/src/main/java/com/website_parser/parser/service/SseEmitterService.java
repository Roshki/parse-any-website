package com.website_parser.parser.service;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
@Service
public class SseEmitterService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    public SseEmitter createEmitter(String reasonGuid) {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> {
            log.info("Received onCompletion request");
            emitter.complete();
        });
        emitter.onError(emitter::completeWithError);
        emitters.put(reasonGuid, emitter);
        return emitter;
    }


    public void sendSse(String data, String reasonGuid) {
        if (emitters.containsKey(reasonGuid)) {
            log.info("sending {} these data: {}", reasonGuid, data);
            SseEmitter sseEmitter = emitters.get(reasonGuid);
            try {
                sseEmitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                completeSseWithError(e, reasonGuid);
                throw new RuntimeException(e);
            }
        }
    }

    public void completeSse(String reasonGuid) {
        if (emitters.containsKey(reasonGuid)) {
            SseEmitter sseEmitter = emitters.get(reasonGuid);
            log.info("completed this emit {}", reasonGuid);
            emitters.remove(reasonGuid);
            sseEmitter.complete();
        }
    }

    public void completeSseWithError(Throwable e, String guid) {
        SseEmitter sseEmitter = emitters.get(guid);
        sseEmitter.completeWithError(e);
        log.error("removed emitter {} with error", guid);
        emitters.remove(guid);

    }
}
