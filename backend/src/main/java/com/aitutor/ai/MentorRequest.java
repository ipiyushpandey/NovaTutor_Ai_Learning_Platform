package com.aitutor.ai;

public record MentorRequest(Long userId, Long courseId, Long lessonId, String mode, String message, String level, String language) {}
