package com.aitutor.config;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="announcements") @Getter @Setter @NoArgsConstructor
public class Announcement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=180) private String title;
    @Column(nullable=false,length=3000) private String message;
    @Column(nullable=false) private boolean published=false;
    @Column(length=40) private String target="ALL";
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    public Announcement(String title,String message,String target,boolean published){this.title=title;this.message=message;this.target=target;this.published=published;}
}
