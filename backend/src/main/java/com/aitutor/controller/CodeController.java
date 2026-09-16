package com.aitutor.controller;

import com.aitutor.ai.AiRequest;
import com.aitutor.ai.AiResponse;
import com.aitutor.ai.AiTask;
import com.aitutor.ai.CodeRequest;
import com.aitutor.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeController {
    private final AiService ai;
    private final SandboxExecutionService sandbox;

    public CodeController(AiService ai, SandboxExecutionService sandbox) {
        this.ai = ai;
        this.sandbox = sandbox;
    }

    @PostMapping("/analyze")
    public AiResponse analyze(@RequestBody CodeRequest request) {
        String mode = request.mode() == null ? "EXPLAIN" : request.mode().toUpperCase();
        String language = request.language() == null ? "Java" : request.language();
        String level = request.level() == null ? "beginner" : request.level();
        String responseLanguage = request.responseLanguage() == null || request.responseLanguage().isBlank() ? "English/Hinglish" : request.responseLanguage();
        String instruction = switch (mode) {
            case "DEBUG" -> "Find bugs first. For every bug explain the cause, the exact fix, and then provide corrected runnable code. Do not invent errors that are not present.";
            case "FIX" -> "Repair the code while preserving its intended behavior. Return the corrected code first, then explain the important changes and edge cases.";
            case "IMPROVE" -> "Review the code for readability, correctness, performance and maintainability. Suggest practical improvements and provide an improved version.";
            case "HINT" -> "Act as a coding tutor. Do not reveal the final solution immediately. Give the smallest useful hint, then a next hint, and explain the key idea.";
            case "PRACTICE" -> "Create one coding practice problem matching the requested language and level. Include constraints, examples and a short hidden-solution approach, but do not reveal full code unless explicitly asked.";
            default -> "Explain the code line-by-line in simple language, then summarize the algorithm, complexity and important edge cases.";
        };

        String prompt = "CODING MODE: " + mode + "\n" + instruction
                + "\n\nIMPORTANT RESPONSE RULES: Do not answer in one sentence. Give a useful, structured mentoring response. For EXPLAIN use: 1) What the code does, 2) Line-by-line / block explanation, 3) Algorithm, 4) Time complexity, 5) Space complexity, 6) Edge cases, 7) One improvement. For DEBUG/FIX identify each concrete issue before the corrected code. For IMPROVE separate correctness, performance and readability. Use Markdown headings and bullets. Put code in fenced code blocks. If the code is already correct, say so and explain why rather than inventing bugs."
                + "\nLanguage: " + language
                + "\nStudent level: " + level
                + "\nResponse language: " + responseLanguage
                + "\nStudent question: " + (request.question() == null ? "No additional question." : request.question())
                + "\n\nCODE:\n" + (request.code() == null ? "(no code supplied)" : request.code());

        return ai.ask(new AiRequest(AiTask.CODE, language, prompt, level, responseLanguage));
    }
    @PostMapping("/execute")
    public java.util.Map<String,Object> execute(@RequestBody ExecuteRequest request) {
        return sandbox.execute(request.language(), request.code(), request.stdin());
    }

    public record ExecuteRequest(String language, String code, String stdin) {}

}

