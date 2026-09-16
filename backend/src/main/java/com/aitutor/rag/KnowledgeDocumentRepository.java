package com.aitutor.rag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument,Long>{
 List<KnowledgeDocument> findAllByOwnerIdOrderByUploadedAtDesc(Long ownerId);
 List<KnowledgeDocument> findAllByOrderByUploadedAtDesc();
 Optional<KnowledgeDocument> findByIdAndOwnerId(Long id, Long ownerId);
}
