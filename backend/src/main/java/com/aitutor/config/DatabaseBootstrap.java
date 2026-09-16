package com.aitutor.config;

import org.springframework.context.annotation.Configuration;

/**
 * V70.44 production safety: the old one-time startup reset has been retired.
 * Database reset must never happen implicitly during application startup.
 * Use an explicit operator-run migration/backup procedure for destructive work.
 */
@Configuration
public class DatabaseBootstrap {
    // Intentionally empty. Kept as a class so older references/documentation do not break.
}
