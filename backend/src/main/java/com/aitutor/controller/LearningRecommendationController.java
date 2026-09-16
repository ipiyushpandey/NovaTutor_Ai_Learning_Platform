package com.aitutor.controller;

import com.aitutor.entity.Course;
import com.aitutor.entity.Lesson;
import com.aitutor.entity.Progress;
import com.aitutor.repository.CourseRepository;
import com.aitutor.repository.LessonRepository;
import com.aitutor.repository.ProgressRepository;
import com.aitutor.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;

import java.util.*;

@RestController
@RequestMapping("/api/learning")
public class LearningRecommendationController {
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final ProgressRepository progress;
    private final UserRepository users;
    private final AuthenticatedUser authenticatedUser;

    public LearningRecommendationController(CourseRepository courses, LessonRepository lessons, ProgressRepository progress, UserRepository users, AuthenticatedUser authenticatedUser) {
        this.courses = courses; this.lessons = lessons; this.progress = progress; this.users = users; this.authenticatedUser = authenticatedUser;
    }

    @GetMapping("/next/{userId}")
    public Map<String, Object> next(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication,userId);
        users.findById(userId).orElseThrow();
        List<Course> all = courses.findAll();
        List<Progress> rows = progress.findByUserId(userId);
        Course chosen = all.stream()
                .min(Comparator.comparingDouble(c -> rows.stream().filter(p -> p.getCourse().getId().equals(c.getId()))
                        .mapToDouble(Progress::getPercent).findFirst().orElse(0)))
                .orElse(null);
        if (chosen == null) return Map.of("available", false, "message", "No course available yet.");
        int done = rows.stream().filter(p -> p.getCourse().getId().equals(chosen.getId())).mapToInt(Progress::getCompletedLessons).findFirst().orElse(0);
        List<Lesson> ls = lessons.findByCourseIdOrderByOrderIndex(chosen.getId());
        Lesson next = done < ls.size() ? ls.get(done) : ls.isEmpty() ? null : ls.get(ls.size() - 1);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("available", true); out.put("course", chosen); out.put("lesson", next);
        out.put("reason", done == 0 ? "Start with the first lesson." : "This is the next lesson in your least-complete course.");
        return out;
    }
}
