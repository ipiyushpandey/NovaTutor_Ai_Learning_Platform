package com.aitutor.controller;

import com.aitutor.config.AnnouncementRepository;
import com.aitutor.entity.User;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementRepository announcements;
    private final AuthenticatedUser authenticatedUser;

    public AnnouncementController(AnnouncementRepository announcements, AuthenticatedUser authenticatedUser) {
        this.announcements = announcements;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public List<AnnouncementNotice> visible(Authentication authentication) {
        User user = authenticatedUser.require(authentication);
        boolean admin = "ADMIN".equalsIgnoreCase(user.getRole()) || "OWNER".equalsIgnoreCase(user.getRole());
        return announcements.findTop50ByPublishedTrueOrderByCreatedAtDesc().stream()
                .filter(a -> admin || targetMatches(a.getTarget(), user.getRole()))
                .map(a -> new AnnouncementNotice(a.getId(), a.getTitle(), a.getMessage(), a.getTarget(), a.getCreatedAt()))
                .toList();
    }

    private boolean targetMatches(String target, String role) {
        String t = target == null ? "ALL" : target.trim().toUpperCase();
        String r = role == null ? "STUDENT" : role.trim().toUpperCase();
        return "ALL".equals(t) || ("STUDENTS".equals(t) && "STUDENT".equals(r));
    }

    public record AnnouncementNotice(Long id, String title, String message, String target, LocalDateTime createdAt) {}
}
