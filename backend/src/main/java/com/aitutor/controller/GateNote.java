package com.aitutor.controller;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_cse_notes")
public class GateNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String subjectKey;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    protected GateNote() {}

    public GateNote(String subjectKey, String filename, String contentType, byte[] data) {
        this.subjectKey = subjectKey;
        this.filename = filename;
        this.contentType = contentType;
        this.data = data;
        this.sizeBytes = data.length;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSubjectKey() { return subjectKey; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public byte[] getData() { return data; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
