package com.aitutor.controller;

import com.aitutor.entity.CommunityPost;
import com.aitutor.entity.User;
import com.aitutor.repository.CommunityPostRepository;
import com.aitutor.repository.UserRepository;
import com.aitutor.security.JwtService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityPostRepository posts;
    private final UserRepository users;
    private final JwtService jwt;

    public CommunityController(CommunityPostRepository posts, UserRepository users, JwtService jwt) {
        this.posts=posts; this.users=users; this.jwt=jwt;
    }

    @GetMapping("/posts")
    public List<Item> posts() {
        return posts.findTop100ByOrderByCreatedAtDesc().stream()
                .map(p -> new Item(p.getId(), p.getUser().getName(), p.getText(), p.getCreatedAt().toString()))
                .toList();
    }

    @PostMapping("/posts")
    public Item create(@RequestHeader(value="Authorization", required=false) String authorization,
                       @RequestBody Request request) {
        if (authorization==null || !authorization.startsWith("Bearer ") || authorization.substring(7).trim().isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        String email;
        try {
            email=jwt.email(authorization.substring(7).trim());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }
        User user=users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        String text=request.text()==null?"":request.text().trim();
        if(text.isBlank()) throw new IllegalArgumentException("Discussion cannot be empty");
        if(text.length()>4000) throw new IllegalArgumentException("Discussion is too long");
        CommunityPost saved=posts.save(new CommunityPost(user,text));
        return new Item(saved.getId(), user.getName(), saved.getText(), saved.getCreatedAt().toString());
    }

    public record Request(String text) {}
    public record Item(Long id,String author,String text,String createdAt) {}
}
