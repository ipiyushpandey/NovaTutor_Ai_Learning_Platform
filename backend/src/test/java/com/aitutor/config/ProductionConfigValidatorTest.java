package com.aitutor.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionConfigValidatorTest {
    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void acceptsSafeProductionConfigurationWithSandboxDisabled() {
        assertDoesNotThrow(() -> new ProductionConfigValidator(
                "jdbc:mysql://db:3306/ai_tutor", "novatutor", "strong-db-password", SECRET,
                "gemini-key", "admin@example.com", "strong-admin-password", "https://app.example.com",
                false, ""));
    }

    @Test
    void rejectsProductionSandboxWithoutExplicitAcknowledgement() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new ProductionConfigValidator(
                "jdbc:mysql://db:3306/ai_tutor", "novatutor", "strong-db-password", SECRET,
                "gemini-key", "admin@example.com", "strong-admin-password", "https://app.example.com",
                true, ""));
        assertTrue(ex.getMessage().contains("CODE_RUNNER_PRODUCTION_ACK"));
    }

    @Test
    void rejectsLocalCorsInProduction() {
        assertThrows(IllegalStateException.class, () -> new ProductionConfigValidator(
                "jdbc:mysql://db:3306/ai_tutor", "novatutor", "strong-db-password", SECRET,
                "gemini-key", "admin@example.com", "strong-admin-password", "http://localhost:5173",
                false, ""));
    }
}
