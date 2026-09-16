package com.aitutor.intelligence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class LearningEventAnalyticsService {
    public static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Kolkata");
    private final LearningEventRepository repository;

    public LearningEventAnalyticsService(LearningEventRepository repository) {
        this.repository = repository;
    }

    public record Summary(long eventCount, long lessonCount, long studySeconds, long xp) {}
    public record DailyActivity(long count, long studySeconds, long xp) {}

    public Summary summary(Long userId) {
        var row = repository.summarize(userId);
        if (row == null) return new Summary(0,0,0,0);
        return new Summary(row.getEventCount(), row.getLessonCount(), row.getStudySeconds(), row.getXp());
    }

    public LinkedHashMap<LocalDate, DailyActivity> last28Days(Long userId) {
        LocalDate today = LocalDate.now(LEARNING_ZONE);
        LocalDate start = today.minusDays(27);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        LinkedHashMap<LocalDate, DailyActivity> result = new LinkedHashMap<>();
        for (int i = 0; i < 28; i++) result.put(start.plusDays(i), new DailyActivity(0,0,0));
        for (var row : repository.findDailyActivity(userId, from, to)) {
            LocalDate date = row.getActivityDate() == null ? null : LocalDate.parse(row.getActivityDate());
            if (date != null && result.containsKey(date)) {
                result.put(date, new DailyActivity(row.getActivityCount(), row.getStudySeconds(), row.getXp()));
            }
        }
        return result;
    }

    public int streak(Long userId) {
        Set<LocalDate> active = new HashSet<>();
        for (java.sql.Date raw : repository.findActiveDates(userId)) {
            LocalDate d = raw == null ? null : raw.toLocalDate();
            if (d != null) active.add(d);
        }
        LocalDate cursor = LocalDate.now(LEARNING_ZONE);
        if (!active.contains(cursor)) cursor = cursor.minusDays(1);
        int streak = 0;
        while (active.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public static boolean countsAsActivity(String type) {
        if (type == null) return false;
        return !Set.of("QUIZ_COMPLETED", "ADAPTIVE_AI_GENERATED", "ADAPTIVE_SHOWN", "DPP_GENERATED").contains(type.toUpperCase(Locale.ROOT));
    }

}
