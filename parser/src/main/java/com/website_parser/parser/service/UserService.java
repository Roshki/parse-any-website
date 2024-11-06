package com.website_parser.parser.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Getter
    private final ConcurrentHashMap<String, Integer> waitingQueue = new ConcurrentHashMap<>();
    private final SseEmitterService sseEmitterService;
    private static final String reasonForMessage = "queue";


    public void notifyQueuePosition(String userGuid, String purpose) {
        try {
            if (waitingQueue.containsKey(userGuid)) {
                int position = waitingQueue.get(userGuid);
                log.info("sending position for: {}", userGuid);
                if (purpose.equals("approval")) {
                    sseEmitterService.sendSse("approve", reasonForMessage + userGuid);
                    return;
                }
                sseEmitterService.sendSse("Your position is: " + position, reasonForMessage + userGuid);
            }
        } catch (Exception e) {
            log.warn("couldn't process the user {}, removing from queue...", userGuid);
            processByGuidInQueue(userGuid);
        }
    }

    public void putToQueue(String userGuid) {
        if (!waitingQueue.containsKey(userGuid)) {
            waitingQueue.put(userGuid, waitingQueue.size() + 1);
        }
    }


    public void processFirstInQueue() {
        waitingQueue.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst().ifPresent(k -> {
                            log.info("removed! {}", k);
                            waitingQueue.remove(k);
                            sseEmitterService.completeSse("queue" + k);
                        }
                );
        waitingQueue.forEach((key, value) -> waitingQueue.put(key, value - 1));
    }

    public void processByGuidInQueue(String guid) {
        waitingQueue.remove(guid);
        waitingQueue.forEach((key, value) -> {
                    if (value > 1) {
                        waitingQueue.put(key, value - 1);
                    } else sseEmitterService.sendSse("you are next", "queue" + key);
                }
        );
    }
}
