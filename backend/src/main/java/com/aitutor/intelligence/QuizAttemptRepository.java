package com.aitutor.intelligence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt,Long>{
 List<QuizAttempt> findTop100ByUserIdOrderBySubmittedAtDesc(Long userId);
 List<QuizAttempt> findTop200ByUserIdOrderBySubmittedAtDesc(Long userId);
 List<QuizAttempt> findByUserId(Long userId);
}
