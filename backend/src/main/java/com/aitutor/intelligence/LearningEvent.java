package com.aitutor.intelligence;

import com.aitutor.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="learning_events", indexes={
        @Index(name="idx_learning_event_user_created", columnList="user_id,createdAt"),
        @Index(name="idx_learning_event_user_type", columnList="user_id,type"),
        @Index(name="ux_learning_event_user_dedupe", columnList="user_id,dedupeKey", unique=true)
})
@Getter @Setter @NoArgsConstructor
public class LearningEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private User user;
    @Column(nullable=false, length=40) private String type;
    @Column(length=120) private String subject;
    @Column(length=180) private String topic;
    private Integer score;
    private Integer durationSeconds;
    @Column(length=1000) private String details;
    @Column(length=180) private String dedupeKey;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    public LearningEvent(User user,String type,String subject,String topic,Integer score,Integer durationSeconds,String details){
        this.user=user; this.type=type; this.subject=subject; this.topic=topic; this.score=score; this.durationSeconds=durationSeconds; this.details=details;
    }

    public LearningEvent(User user,String type,String subject,String topic,Integer score,Integer durationSeconds,String details,String dedupeKey){
        this(user,type,subject,topic,score,durationSeconds,details);
        this.dedupeKey=dedupeKey;
    }
}
