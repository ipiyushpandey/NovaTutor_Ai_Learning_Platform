package com.aitutor.intelligence;
import com.aitutor.entity.Course;
import com.aitutor.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="quiz_attempts", indexes={
        @Index(name="idx_quiz_attempt_user", columnList="user_id,submittedAt"),
        @Index(name="idx_quiz_attempt_course", columnList="course_id,submittedAt")
})
@Getter @Setter @NoArgsConstructor
public class QuizAttempt {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) private User user;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) private Course course;
 private int total;
 private int correct;
 private int score;
 @Column(length=1000) private String wrongQuestionIds;
 @Column(nullable=false) private LocalDateTime submittedAt=LocalDateTime.now();
 public QuizAttempt(User user,Course course,int total,int correct,int score,String wrongQuestionIds){this.user=user;this.course=course;this.total=total;this.correct=correct;this.score=score;this.wrongQuestionIds=wrongQuestionIds;}
}
