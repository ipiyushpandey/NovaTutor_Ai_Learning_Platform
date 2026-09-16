package com.aitutor.service;

import com.aitutor.ai.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class AiService {
    private final List<AiProvider> providers;
    public AiService(List<AiProvider> providers){this.providers=providers;}

    public AiResponse ask(AiRequest r){
        return providers.stream().filter(AiProvider::available).findFirst()
                .map(p->p.generate(r))
                .orElse(new AiResponse("No AI provider configured. Add GEMINI_API_KEY to backend environment.",r.task().name(),"none"));
    }

    public void stream(AiRequest r, Consumer<String> onChunk) throws Exception {
        for (AiProvider provider : providers) {
            if (!provider.available()) continue;
            if (provider instanceof GeminiProvider gemini) {
                gemini.stream(r, onChunk);
                return;
            }
        }
        throw new IllegalStateException("No AI provider configured. Add GEMINI_API_KEY to backend environment.");
    }
}
