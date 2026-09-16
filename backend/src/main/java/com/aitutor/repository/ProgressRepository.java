package com.aitutor.repository;

import com.aitutor.entity.Progress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProgressRepository extends JpaRepository<Progress,Long> {
    @Query("select p from Progress p join fetch p.course where p.user.id = :userId")
    List<Progress> findByUserId(@Param("userId") Long userId);
    Optional<Progress> findByUserIdAndCourseId(Long userId,Long courseId);
    @Modifying @Query("delete from Progress p where p.course.id = :courseId") void deleteByCourseId(@Param("courseId") Long courseId);
    @Modifying @Query("delete from Progress p where p.user.id = :userId") void deleteByUserId(@Param("userId") Long userId);
}
