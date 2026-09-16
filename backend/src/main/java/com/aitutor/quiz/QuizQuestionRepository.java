package com.aitutor.quiz;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByCourseIdOrderByIdAsc(Long courseId);
    @Modifying @Query("delete from QuizQuestion q where q.course.id = :courseId") void deleteByCourseId(@Param("courseId") Long courseId);
}
