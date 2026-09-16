package com.aitutor.rag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk,Long>{ List<KnowledgeChunk> findAllByDocumentIdOrderByChunkIndex(Long documentId); }
