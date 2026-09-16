package com.aitutor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="community_posts", indexes={@Index(name="idx_community_posts_created", columnList="createdAt")})
@Getter @Setter @NoArgsConstructor
public class CommunityPost {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false, length=4000)
    private String text;

    @Column(nullable=false)
    private LocalDateTime createdAt=LocalDateTime.now();

    public CommunityPost(User user, String text) { this.user=user; this.text=text; }
}
