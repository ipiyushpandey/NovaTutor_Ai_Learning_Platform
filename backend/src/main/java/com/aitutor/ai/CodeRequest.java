package com.aitutor.ai;

public record CodeRequest(
        String language,
        String mode,
        String code,
        String question,
        String level,
        String responseLanguage
) {}
