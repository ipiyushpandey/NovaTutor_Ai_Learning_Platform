package com.aitutor.repository;
import com.aitutor.entity.Lesson; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface LessonRepository extends JpaRepository<Lesson,Long>{
    List<Lesson> findByCourseIdOrderByOrderIndex(Long id);
    List<Lesson> findByCourseIdOrderById(Long id);
    @Modifying @Query("delete from Lesson l where l.course.id = :courseId") void deleteByCourseId(@Param("courseId") Long courseId);
}
