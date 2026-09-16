package com.aitutor.controller;

import com.aitutor.ai.*;
import com.aitutor.entity.Lesson;
import com.aitutor.entity.Progress;
import com.aitutor.entity.User;
import com.aitutor.repository.LessonRepository;
import com.aitutor.repository.ProgressRepository;
import com.aitutor.repository.UserRepository;
import com.aitutor.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.aitutor.security.AuthenticatedUser;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/mentor")
public class MentorController {
    private final AiService ai;
    private final LessonRepository lessons;
    private final ProgressRepository progress;
    private final UserRepository users;
    private final AuthenticatedUser authenticatedUser;

    public MentorController(AiService ai, LessonRepository lessons, ProgressRepository progress, UserRepository users, AuthenticatedUser authenticatedUser) {
        this.ai = ai;
        this.lessons = lessons;
        this.progress = progress;
        this.users = users;
        this.authenticatedUser = authenticatedUser;
    }

    @PostMapping("/ask")
    public AiResponse ask(@RequestBody MentorRequest r, Authentication authentication) {
        Lesson lesson = lessons.findById(r.lessonId()).orElseThrow();
        User authenticated = authenticatedUser.require(authentication);
        return ai.ask(buildRequest(r, lesson, authenticated));
    }

    /** Fast AI Teacher transport: text is forwarded as Gemini generates it. */
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@RequestBody MentorRequest r, Authentication authentication) {
        Lesson lesson = lessons.findById(r.lessonId()).orElseThrow();
        User authenticated = authenticatedUser.require(authentication);
        SseEmitter emitter = new SseEmitter(300_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> emitter.complete());
        AiRequest request = buildRequest(r, lesson, authenticated);
        new Thread(() -> {
            try {
                ai.stream(request, chunk -> {
                    try { emitter.send(SseEmitter.event().data(chunk)); }
                    catch (IOException e) { throw new IllegalStateException(e); }
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

    private AiRequest buildRequest(MentorRequest r, Lesson lesson, User user) {
        int completed = 0;
        if (r.courseId() != null) {
            completed = progress.findByUserIdAndCourseId(user.getId(), r.courseId())
                    .map(Progress::getCompletedLessons).orElse(0);
        }
        String mode = r.mode() == null ? "TEACH" : r.mode().toUpperCase(Locale.ROOT);
        String instruction = switch (mode) {
            case "SIMPLIFY" -> "Simplify this lesson for a beginner using an everyday analogy and one tiny example.";
            case "EXAMPLE" -> "Teach this lesson through one practical example. Explain each step and then ask one check question.";
            case "HINT" -> "Give a hint for the student's question. Guide them with the smallest useful next step.";
            case "CHECK" -> "Act as a mini oral examiner. Ask one question based on this lesson and evaluate the student's answer if provided.";
            default -> "Answer the student's question first, then teach the next useful concept step-by-step. End with one short check question.";
        };
        String student = limit(r.message(), 6000, "Start teaching me.");
        // Do not send an entire oversized lesson on every AI request. The lesson title,
        // a focused content window and the learner question are enough for the fast path.
        String lessonContent = limit(lesson.getContent(), 14000, "Lesson content is unavailable.");
        String context = "Student goal=" + safe(user.getLearningGoal())
                + ", learning style=" + safe(user.getLearningStyle())
                + ", weaknesses=" + safe(user.getWeaknesses())
                + "\nLesson title: " + safe(lesson.getTitle())
                + "\nLesson content:\n" + lessonContent
                + "\nCourse completed lessons: " + completed
                + "\nMode: " + mode + "\nTutor instruction: " + instruction
                + "\nTeaching rule: use short sections, concrete examples and visual mental models only when they improve understanding."
                + "\nStudent message: " + student;
        return new AiRequest(AiTask.ADAPTIVE_TUTOR, lesson.getTitle(), context, r.level(), r.language());
    }

    private String limit(String value, int max, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max) + "\n[Lesson content shortened for response speed.]";
    }

    private String safe(String x){ return x==null||x.isBlank()?"not specified":limit(x,500,"not specified"); }

    @GetMapping("/state/{userId}")
    public Map<String, Object> state(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication,userId);
        User user = users.findById(userId).orElseThrow();
        List<Progress> rows = progress.findByUserId(user.getId());
        double average = rows.stream().mapToDouble(Progress::getPercent).average().orElse(0);
        String level = average < 30 ? "beginner" : average < 70 ? "intermediate" : "advanced";
        return Map.of("userId", userId, "averageProgress", Math.round(average * 10.0) / 10.0, "recommendedLevel", level,
                "coursesStarted", rows.size(), "completedLessons", rows.stream().mapToInt(Progress::getCompletedLessons).sum());
    }
}
