package com.aitutor.service;

import com.aitutor.entity.User;
import com.aitutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final String adminEmail;

    public AuthService(UserRepository r, PasswordEncoder e, @Value("${app.admin.email:}") String adminEmail) {
        repo = r; encoder = e; this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
    }

    public User register(String name, String email, String password) {
        String cleanEmail = normalizeEmail(email);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
        if (repo.findByEmail(cleanEmail).isPresent()) throw new IllegalArgumentException("Email already registered");
        User u = new User(name.trim(), cleanEmail, encoder.encode(password));
        if (!adminEmail.isBlank()) {
            if (adminEmail.equals(cleanEmail)) u.setRole("ADMIN");
        } else if (repo.count() == 0 || repo.findAll().stream().noneMatch(x -> "ADMIN".equalsIgnoreCase(x.getRole()))) {
            // First account becomes the owner when ADMIN_EMAIL is not configured.
            u.setRole("ADMIN");
        }
        return repo.save(u);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return rawPassword != null && encodedPassword != null && encoder.matches(rawPassword, encodedPassword);
    }

    public User find(String email) {
        return repo.findByEmail(normalizeEmail(email)).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }

    public User syncOwnerRole(User user) {
        if (!adminEmail.isBlank()) {
            if (adminEmail.equals(user.getEmail()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                user.setRole("ADMIN");
                return repo.save(user);
            }
            return user;
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) return user;
        boolean anyAdmin = repo.findAll().stream().anyMatch(x -> "ADMIN".equalsIgnoreCase(x.getRole()));
        if (!anyAdmin && repo.count() > 0) {
            user.setRole("ADMIN");
            return repo.save(user);
        }
        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        return email.trim().toLowerCase();
    }
}
