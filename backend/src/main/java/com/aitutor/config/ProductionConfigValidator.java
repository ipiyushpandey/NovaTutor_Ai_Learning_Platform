package com.aitutor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

/** Fails fast on missing production secrets/configuration. */
@Configuration
@Profile("prod")
public class ProductionConfigValidator {
    public ProductionConfigValidator(
            @Value("${DB_URL:}") String dbUrl,
            @Value("${DB_USERNAME:}") String dbUsername,
            @Value("${DB_PASSWORD:}") String dbPassword,
            @Value("${JWT_SECRET:}") String jwtSecret,
            @Value("${GEMINI_API_KEY:}") String geminiKey,
            @Value("${ADMIN_EMAIL:}") String adminEmail,
            @Value("${ADMIN_PASSWORD:}") String adminPassword,
            @Value("${CORS_ORIGINS:}") String corsOrigins,
            @Value("${CODE_RUNNER_ENABLED:false}") boolean codeRunnerEnabled,
            @Value("${CODE_RUNNER_PRODUCTION_ACK:}") String codeRunnerProductionAck) {
        require("DB_URL", dbUrl);
        require("DB_USERNAME", dbUsername);
        require("DB_PASSWORD", dbPassword);
        require("JWT_SECRET", jwtSecret);
        require("GEMINI_API_KEY", geminiKey);
        require("ADMIN_EMAIL", adminEmail);
        require("ADMIN_PASSWORD", adminPassword);
        require("CORS_ORIGINS", corsOrigins);
        if (jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes in production");
        }
        if (corsOrigins.contains("localhost") || corsOrigins.contains("127.0.0.1") || corsOrigins.contains("[::1]")) {
            throw new IllegalStateException("CORS_ORIGINS must not contain local development origins in production");
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters in production");
        }
        if (codeRunnerEnabled && !"I_UNDERSTAND_SANDBOX_RISK".equals(codeRunnerProductionAck)) {
            throw new IllegalStateException("CODE_RUNNER_PRODUCTION_ACK must equal I_UNDERSTAND_SANDBOX_RISK before enabling the production sandbox");
        }
    }

    private static void require(String name, String value) {
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " must be configured in the prod profile");
    }
}
