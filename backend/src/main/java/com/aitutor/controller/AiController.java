package com.aitutor.controller;

import com.aitutor.ai.*;
import com.aitutor.service.AiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService ai;
    private final List<AiProvider> providers;

    public AiController(AiService a, List<AiProvider> providers){
        this.ai = a;
        this.providers = providers;
    }

    @PostMapping("/ask")
    public AiResponse ask(@RequestBody AiRequest r){
        return ai.ask(r);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody AiRequest r) {
        SseEmitter emitter = new SseEmitter(300_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> emitter.complete());
        new Thread(() -> {
            try {
                ai.stream(r, chunk -> {
                    try { emitter.send(SseEmitter.event().data(chunk)); }
                    catch (java.io.IOException e) { throw new IllegalStateException(e); }
                });
                emitter.send(SseEmitter.event().name("done").data("done"));
                emitter.complete();
            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().name("error").data("AI request failed. Please try again.")); }
                catch (Exception ignored) { }
                emitter.completeWithError(e);
            }
        }, "nova-ai-stream").start();
        return emitter;
    }

    @GetMapping("/status")
    public Map<String,Object> status(){
        AiProvider provider = providers.stream()
                .filter(AiProvider::available)
                .findFirst()
                .orElse(null);

        String model = "—";
        if (provider instanceof GeminiProvider gemini) model = gemini.model();

        return Map.of(
                "configured", provider != null,
                "provider", provider == null ? "none" : provider.name(),
                "model", model
        );
    }
}
