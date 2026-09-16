package com.aitutor.intelligence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface LearningEventRepository extends JpaRepository<LearningEvent,Long>{
    List<LearningEvent> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
    List<LearningEvent> findTop200ByUserIdOrderByCreatedAtDesc(Long userId);
    List<LearningEvent> findTop8ByUserIdOrderByCreatedAtDesc(Long userId);
    List<LearningEvent> findTop5ByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
    List<LearningEvent> findByUserIdOrderByCreatedAtAsc(Long userId);
    long countByUserIdAndType(Long userId,String type);
    long countByUserIdAndTypeAndScoreGreaterThanEqual(Long userId,String type,Integer score);
    Optional<LearningEvent> findByUserIdAndDedupeKey(Long userId,String dedupeKey);

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO learning_events
            (user_id, type, subject, topic, score, duration_seconds, details, dedupe_key, created_at)
        VALUES (:userId, :type, :subject, :topic, :score, :durationSeconds, :details, :dedupeKey, :createdAt)
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("subject") String subject,
            @Param("topic") String topic,
            @Param("score") Integer score,
            @Param("durationSeconds") Integer durationSeconds,
            @Param("details") String details,
            @Param("dedupeKey") String dedupeKey,
            @Param("createdAt") java.time.LocalDateTime createdAt);

    interface DailyActivityProjection {
        String getActivityDate();
        long getActivityCount();
        long getStudySeconds();
        long getXp();
    }

    interface LeaderboardXpProjection {
        Long getUserId();
        long getXp();
    }

    @Query(value = """
        SELECT user_id AS userId, COALESCE(SUM(
          CASE UPPER(type)
            WHEN 'LESSON_COMPLETED' THEN 10
            WHEN 'QUIZ_QUESTION' THEN 5
            WHEN 'QUIZ_COMPLETED' THEN 20
            WHEN 'DPP_COMPLETED' THEN 25
            WHEN 'ADAPTIVE_QUESTION' THEN 12
            WHEN 'REVISION_COMPLETED' THEN 15
            WHEN 'STUDY_SPRINT_COMPLETED' THEN 10
            WHEN 'STUDY_SESSION_COMPLETED' THEN 10
            WHEN 'CHALLENGE_COMPLETED' THEN 40
            WHEN 'TEST_COMPLETED' THEN 35
            WHEN 'EXAM_COMPLETED' THEN 35
            WHEN 'INTERVIEW_COMPLETED' THEN 30
            ELSE 0
          END + CASE WHEN score IS NULL THEN 0 ELSE GREATEST(0, LEAST(10, FLOOR(score/10))) END
        ),0) AS xp
        FROM learning_events
        WHERE UPPER(type) NOT IN ('ADAPTIVE_AI_GENERATED','ADAPTIVE_SHOWN','DPP_GENERATED')
        GROUP BY user_id
        ORDER BY xp DESC
        LIMIT 20
        """, nativeQuery = true)
    List<LeaderboardXpProjection> leaderboardXp();

    interface SummaryProjection {
        long getEventCount();
        long getLessonCount();
        long getStudySeconds();
        long getXp();
    }

    @Query(value = """
        SELECT DATE(created_at) AS activityDate,
               COUNT(*) AS activityCount,
               COALESCE(SUM(COALESCE(duration_seconds,0)),0) AS studySeconds,
               COALESCE(SUM(
                 CASE UPPER(type)
                   WHEN 'LESSON_COMPLETED' THEN 10
                   WHEN 'QUIZ_QUESTION' THEN 5
                   WHEN 'QUIZ_COMPLETED' THEN 20
                   WHEN 'DPP_COMPLETED' THEN 25
                   WHEN 'ADAPTIVE_QUESTION' THEN 12
                   WHEN 'REVISION_COMPLETED' THEN 15
                   WHEN 'STUDY_SPRINT_COMPLETED' THEN 10
                   WHEN 'STUDY_SESSION_COMPLETED' THEN 10
                   WHEN 'CHALLENGE_COMPLETED' THEN 40
                   WHEN 'TEST_COMPLETED' THEN 35
                   WHEN 'EXAM_COMPLETED' THEN 35
                   WHEN 'INTERVIEW_COMPLETED' THEN 30
                   ELSE 0
                 END +
                 CASE WHEN score IS NULL THEN 0 ELSE GREATEST(0, LEAST(10, FLOOR(score/10))) END
               ),0) AS xp
        FROM learning_events
        WHERE user_id = :userId
          AND created_at >= :fromDate
          AND created_at < :toDate
          AND UPPER(type) NOT IN ('QUIZ_COMPLETED','ADAPTIVE_AI_GENERATED','ADAPTIVE_SHOWN','DPP_GENERATED')
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
        """, nativeQuery = true)
    List<DailyActivityProjection> findDailyActivity(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
        SELECT COALESCE(SUM(CASE WHEN UPPER(type) NOT IN ('QUIZ_COMPLETED','ADAPTIVE_AI_GENERATED','ADAPTIVE_SHOWN','DPP_GENERATED') THEN 1 ELSE 0 END),0) AS eventCount,
               COALESCE(SUM(CASE WHEN UPPER(type) = 'LESSON_COMPLETED' THEN 1 ELSE 0 END),0) AS lessonCount,
               COALESCE(SUM(COALESCE(duration_seconds,0)),0) AS studySeconds,
               COALESCE(SUM(
                 CASE UPPER(type)
                   WHEN 'LESSON_COMPLETED' THEN 10
                   WHEN 'QUIZ_QUESTION' THEN 5
                   WHEN 'QUIZ_COMPLETED' THEN 20
                   WHEN 'DPP_COMPLETED' THEN 25
                   WHEN 'ADAPTIVE_QUESTION' THEN 12
                   WHEN 'REVISION_COMPLETED' THEN 15
                   WHEN 'STUDY_SPRINT_COMPLETED' THEN 10
                   WHEN 'STUDY_SESSION_COMPLETED' THEN 10
                   WHEN 'CHALLENGE_COMPLETED' THEN 40
                   WHEN 'TEST_COMPLETED' THEN 35
                   WHEN 'EXAM_COMPLETED' THEN 35
                   WHEN 'INTERVIEW_COMPLETED' THEN 30
                   ELSE 0
                 END +
                 CASE WHEN score IS NULL THEN 0 ELSE GREATEST(0, LEAST(10, FLOOR(score/10))) END
               ),0) AS xp
        FROM learning_events
        WHERE user_id = :userId
        """, nativeQuery = true)
    SummaryProjection summarize(@Param("userId") Long userId);

    @Query(value = """
        SELECT DISTINCT DATE(created_at)
        FROM learning_events
        WHERE user_id = :userId
          AND UPPER(type) NOT IN ('QUIZ_COMPLETED','ADAPTIVE_AI_GENERATED','ADAPTIVE_SHOWN','DPP_GENERATED')
        ORDER BY DATE(created_at) DESC
        """, nativeQuery = true)
    List<java.sql.Date> findActiveDates(@Param("userId") Long userId);
}
