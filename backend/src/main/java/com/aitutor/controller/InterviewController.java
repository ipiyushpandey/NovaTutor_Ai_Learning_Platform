package com.aitutor.controller;

import com.aitutor.ai.*;
import com.aitutor.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {
    private final AiService ai;
    public InterviewController(AiService ai){ this.ai = ai; }

    @PostMapping("/ask")
    public AiResponse ask(@RequestBody InterviewRequest request){
        String prompt = "Interview type: " + safe(request.type()) +
                "\nRole: " + safe(request.role()) +
                "\nSubject: " + safe(request.subject()) +
                "\nInterview mode: " + safe(request.mode()) +
                "\nDifficulty: " + safe(request.difficulty()) +
                "\nQuestion number: " + request.questionNumber() +
                "\nInterview length: 10 questions." +
                "\nCandidate answer: " + safe(request.answer()) +
                "\nInstructions: Run a realistic interview. If a candidate answer is supplied, first give concise feedback with strength, gap, and an explicit score out of 10, then ask exactly one next question. If no answer is supplied, ask only the first question. Adapt the question to the selected subject, mode, role and difficulty. Do not reveal a full model answer unless specifically requested. Keep each turn concise and practical.";
        return ai.ask(new AiRequest(AiTask.INTERVIEW, request.subject() == null ? request.role() : request.subject(), prompt, request.difficulty(), request.language() == null || request.language().isBlank() ? "English/Hinglish" : request.language()));
    }
    private String safe(String s){ return s == null ? "not specified" : s; }
}
