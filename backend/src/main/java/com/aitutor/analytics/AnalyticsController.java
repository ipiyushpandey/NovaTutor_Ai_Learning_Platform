package com.aitutor.analytics;

import com.aitutor.entity.Progress;
import com.aitutor.repository.ProgressRepository;
import com.aitutor.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser; private final com.aitutor.intelligence.LearningEventAnalyticsService learningAnalytics;

    public AnalyticsController(ProgressRepository progressRepository, UserRepository userRepository, AuthenticatedUser authenticatedUser, com.aitutor.intelligence.LearningEventAnalyticsService learningAnalytics) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
        this.learningAnalytics = learningAnalytics;
    }

    public record CourseStat(Long courseId, String title, int completedLessons, int totalLessons, double percent) {}
    public record AnalyticsView(int coursesStarted, int lessonsCompleted, int lessonsAvailable,
                                double overallProgress, int xp, int level, List<String> badges,
                                List<CourseStat> courses) {}

    @GetMapping("/{userId}")
    @Transactional(readOnly = true)
    AnalyticsView analytics(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication, userId);
        if (!userRepository.existsById(userId)) {
            return new AnalyticsView(0, 0, 0, 0, 0, 1, List.of(), List.of());
        }
        List<Progress> rows = progressRepository.findByUserId(userId);

        int completed = rows.stream().mapToInt(Progress::getCompletedLessons).sum();
        int available = rows.stream().mapToInt(Progress::getTotalLessons).sum();
        double overall = available == 0 ? 0 : completed * 100.0 / available;
        int xp = (int)Math.min(Integer.MAX_VALUE, learningAnalytics.summary(userId).xp());
        int level = Math.max(1, 1 + xp / 500);

        List<String> badges = new ArrayList<>();
        if (completed >= 1) badges.add("First Step");
        if (completed >= 5) badges.add("Fast Learner");
        if (overall >= 50) badges.add("Halfway Hero");
        if (overall >= 100) badges.add("Course Finisher");
        if (rows.size() >= 2) badges.add("Multi-Skilled");

        List<CourseStat> courses = rows.stream()
                .map(p -> new CourseStat(p.getCourse().getId(), p.getCourse().getTitle(),
                        p.getCompletedLessons(), p.getTotalLessons(), p.getPercent()))
                .sorted(Comparator.comparingDouble(CourseStat::percent).reversed())
                .toList();

        return new AnalyticsView(rows.size(), completed, available, overall, xp, level, badges, courses);
    }

}
