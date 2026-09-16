package com.aitutor.chat;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface MessageRepository extends JpaRepository<ChatMessage,Long>{
    List<ChatMessage> findTop20ByConversationIdOrderByCreatedAtDesc(Long conversationId);
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
