package com.aitutor.quiz;

import com.aitutor.entity.Course;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class QuizQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Course course;
    @Column(nullable = false, length = 500) private String question;
    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;
    @Column(nullable = false) private int correctOption;
    @Column(length = 1000) private String explanation;
    @Column(length = 120) private String topic;
    private String difficulty;
}
