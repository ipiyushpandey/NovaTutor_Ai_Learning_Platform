package com.aitutor.controller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GateNoteRepository extends JpaRepository<GateNote, Long> {
    List<GateNote> findAllByOrderBySubjectKeyAscFilenameAsc();
    Optional<GateNote> findByFilename(String filename);
}
