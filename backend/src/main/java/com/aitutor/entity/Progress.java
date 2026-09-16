package com.aitutor.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","course_id"}))
public class Progress { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(optional=false) User user; @ManyToOne(optional=false) Course course; int completedLessons; int totalLessons; double percent; }
