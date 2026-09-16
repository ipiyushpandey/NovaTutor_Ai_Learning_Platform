package com.aitutor.config;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}
