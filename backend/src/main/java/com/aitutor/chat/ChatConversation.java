package com.aitutor.chat;
import com.aitutor.entity.User; import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="chat_conversations")
public class ChatConversation { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(optional=false) private User user; private String title; private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime updatedAt=LocalDateTime.now(); public Long getId(){return id;} public User getUser(){return user;} public void setUser(User u){user=u;} public String getTitle(){return title;} public void setTitle(String t){title=t;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void touch(){updatedAt=LocalDateTime.now();} }
