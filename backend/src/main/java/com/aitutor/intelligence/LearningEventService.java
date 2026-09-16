package com.aitutor.intelligence;

import com.aitutor.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningEventService {
    private final LearningEventRepository repository;

    public LearningEventService(LearningEventRepository repository) { this.repository = repository; }

    @Transactional
    public LearningEvent record(User user, String type, String subject, String topic, Integer score, Integer durationSeconds, String details) {
        return record(user, type, subject, topic, score, durationSeconds, details, null);
    }

    @Transactional
    public LearningEvent record(User user, String type, String subject, String topic, Integer score, Integer durationSeconds, String details, String dedupeKey) {
        if (user == null) throw new IllegalArgumentException("Learning event user is required");
        String normalizedType = type == null ? "" : type.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalizedType.isBlank()) throw new IllegalArgumentException("Learning event type is required");
        if (dedupeKey != null && !dedupeKey.isBlank()) {
            String key = dedupeKey.trim();
            var existing = repository.findByUserIdAndDedupeKey(user.getId(), key);
            if (existing.isPresent()) return existing.get();
            repository.insertIfAbsent(user.getId(), normalizedType, subject, topic, score, durationSeconds, details, key, java.time.LocalDateTime.now(LearningEventAnalyticsService.LEARNING_ZONE));
            return repository.findByUserIdAndDedupeKey(user.getId(), key)
                    .orElseThrow(() -> new IllegalStateException("Learning event could not be recorded"));
        }
        return repository.save(new LearningEvent(user, normalizedType, subject, topic, score, durationSeconds, details));
    }
}
