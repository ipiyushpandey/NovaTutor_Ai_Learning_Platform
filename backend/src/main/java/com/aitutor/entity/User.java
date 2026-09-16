package com.aitutor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true) private String email;
 @Column(nullable=false) private String password;
 @Column(nullable=false) private String name;
 @Column(nullable=false) private String role="STUDENT";
 private LocalDateTime createdAt=LocalDateTime.now();
 private String educationLevel;
 private String learningGoal;
 private String targetExam;
 @Column(length=1000) private String interests;
 @Column(length=1000) private String strengths;
 @Column(length=1000) private String weaknesses;
 private String learningStyle;
 private Integer dailyStudyMinutes;
 private String preferredAiLanguage;
 @Column(nullable=false, columnDefinition="boolean default true") private boolean active=true;
 @Column(nullable=false, columnDefinition="boolean default false") private boolean blocked=false;
 public User(String name,String email,String password){this.name=name;this.email=email;this.password=password;}
}
