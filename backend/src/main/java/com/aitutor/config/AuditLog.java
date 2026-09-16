package com.aitutor.config;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="admin_audit_logs", indexes={
        @Index(name="idx_admin_audit_created", columnList="createdAt"),
        @Index(name="idx_admin_audit_action", columnList="action")
})
@Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=180) private String adminEmail;
    @Column(nullable=false, length=80) private String action;
    @Column(length=120) private String target;
    @Column(length=40) private String status;
    @Column(length=1000) private String details;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    public AuditLog(String adminEmail,String action,String target,String status,String details){this.adminEmail=adminEmail;this.action=action;this.target=target;this.status=status;this.details=details;}
}
