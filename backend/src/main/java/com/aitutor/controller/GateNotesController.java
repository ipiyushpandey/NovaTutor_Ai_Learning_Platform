package com.aitutor.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gate-cse/notes")
public class GateNotesController {
    private final GateNoteRepository gateNotes;

    public GateNotesController(GateNoteRepository gateNotes) {
        this.gateNotes = gateNotes;
    }

    @GetMapping
    public List<Map<String,Object>> listNotes() {
        return gateNotes.findAllByOrderBySubjectKeyAscFilenameAsc().stream()
                .map(this::view)
                .toList();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getNote(@PathVariable String filename) {
        String safe = java.nio.file.Paths.get(filename).getFileName().toString();
        if (!safe.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            return ResponseEntity.badRequest().build();
        }
        return gateNotes.findByFilename(safe)
                .map(note -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", "inline; filename=\"" + safe + "\"")
                        .body(note.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String,Object> view(GateNote note) {
        Map<String,Object> view = new LinkedHashMap<>();
        view.put("filename", note.getFilename());
        view.put("subject", note.getSubjectKey().replace('-', ' '));
        view.put("subjectKey", note.getSubjectKey());
        view.put("sizeBytes", note.getSizeBytes());
        view.put("uploadedAt", note.getUploadedAt());
        view.put("url", "/api/gate-cse/notes/" + note.getFilename());
        return view;
    }
}
