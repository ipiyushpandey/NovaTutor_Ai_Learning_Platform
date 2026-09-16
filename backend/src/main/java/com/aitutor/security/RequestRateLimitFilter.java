package com.aitutor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Small dependency-free sliding-window limiter for public API protection.
 * Production deployments should additionally place a trusted reverse-proxy/WAF
 * rate limit in front of the application.
 */
public class RequestRateLimitFilter extends OncePerRequestFilter {
    private record Window(long startedAt, int count) {}
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final boolean enabled;
    private final int generalRpm, authRpm, aiRpm, codeRpm, upload10m, adminRpm;
    private final boolean trustProxy;

    public RequestRateLimitFilter(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.requests-per-minute:120}") int generalRpm,
            @Value("${app.rate-limit.auth-per-minute:10}") int authRpm,
            @Value("${app.rate-limit.ai-per-minute:20}") int aiRpm,
            @Value("${app.rate-limit.code-per-minute:20}") int codeRpm,
            @Value("${app.rate-limit.upload-per-10-minutes:5}") int upload10m,
            @Value("${app.rate-limit.admin-per-minute:600}") int adminRpm,
            @Value("${app.rate-limit.trust-proxy:false}") boolean trustProxy) {
        this.enabled = enabled;
        this.generalRpm = Math.max(10, generalRpm);
        this.authRpm = Math.max(3, authRpm);
        this.aiRpm = Math.max(5, aiRpm);
        this.codeRpm = Math.max(5, codeRpm);
        this.upload10m = Math.max(1, upload10m);
        this.adminRpm = Math.max(60, adminRpm);
        this.trustProxy = trustProxy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())) { chain.doFilter(request, response); return; }
        String path = request.getRequestURI();
        int limit = generalRpm; long windowMs = 60_000L; String bucket = "general";
        if (path.startsWith("/api/auth/")) { limit = authRpm; bucket = "auth"; }
        else if (path.contains("/upload") || path.startsWith("/api/admin/gate-notes")) { limit = upload10m; windowMs = 600_000L; bucket = "upload"; }
        else if (path.startsWith("/api/admin/")) { limit = adminRpm; bucket = "admin"; }
        else if (path.startsWith("/api/ai/")) { limit = aiRpm; bucket = "ai"; }
        else if (path.startsWith("/api/code/")) { limit = codeRpm; bucket = "code"; }
        String key = bucket + ":" + rateLimitIdentity(request);
        long now = Instant.now().toEpochMilli();
        final long activeWindowMs = windowMs;
        windows.compute(key, (k, old) -> {
            if (old == null || now - old.startedAt() >= activeWindowMs) return new Window(now, 1);
            return new Window(old.startedAt(), old.count() + 1);
        });
        Window w = windows.get(key);
        if (w != null && w.count() > limit) {
            long retry = Math.max(1, (activeWindowMs - (now - w.startedAt())) / 1000L);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(retry));
            response.getWriter().write("{\"error\":\"RATE_LIMITED\",\"message\":\"Too many requests. Please retry later.\"}");
            return;
        }
        // Opportunistic cleanup prevents abandoned client/user keys from growing forever.
        if (windows.size() > 5000) windows.entrySet().removeIf(e -> now - e.getValue().startedAt() > 600_000L);
        chain.doFilter(request, response);
    }

    private String rateLimitIdentity(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null
                && !authentication.getName().isBlank() && !"anonymousUser".equals(authentication.getName())) {
            return "user:" + authentication.getName().trim().toLowerCase(java.util.Locale.ROOT);
        }
        return "ip:" + clientAddress(request);
    }

    private String clientAddress(HttpServletRequest request) {
        if (trustProxy) {
            String forwarded = request.getHeader("X-Real-IP");
            if (forwarded != null && !forwarded.isBlank()) return forwarded.trim();
            String chain = request.getHeader("X-Forwarded-For");
            if (chain != null && !chain.isBlank()) return chain.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
