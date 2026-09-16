package com.aitutor.rag;

import jakarta.persistence.*;

@Entity
@Table(name="knowledge_chunks")
public class KnowledgeChunk {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="document_id") public KnowledgeDocument document;
 @Column(nullable=false) public int chunkIndex;
 @Column(nullable=false) public int pageNumber;
 @Column(nullable=false, columnDefinition="LONGTEXT") public String content;
 public KnowledgeChunk() {}
 public KnowledgeChunk(KnowledgeDocument d,int i,int page,String content){this.document=d;this.chunkIndex=i;this.pageNumber=page;this.content=content;}
}
