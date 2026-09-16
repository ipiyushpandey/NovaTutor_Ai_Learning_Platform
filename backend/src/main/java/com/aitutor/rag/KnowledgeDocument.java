package com.aitutor.rag;

import jakarta.persistence.*;
import com.aitutor.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name="knowledge_documents")
public class KnowledgeDocument {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String fileName;
 @Column(nullable=false) public String contentType;
 @Column(nullable=false) public long sizeBytes;
 @Column(nullable=false, columnDefinition="LONGTEXT") public String extractedText;
 @Column(nullable=false) public LocalDateTime uploadedAt;
 @ManyToOne(fetch=FetchType.LAZY, optional=true) @JoinColumn(name="owner_id") public User owner;
 public KnowledgeDocument() {}
 public KnowledgeDocument(String fileName,String contentType,long sizeBytes,String extractedText){this(fileName,contentType,sizeBytes,extractedText,null);}
 public KnowledgeDocument(String fileName,String contentType,long sizeBytes,String extractedText,User owner){this.fileName=fileName;this.contentType=contentType;this.sizeBytes=sizeBytes;this.extractedText=extractedText;this.uploadedAt=LocalDateTime.now();this.owner=owner;}
}
