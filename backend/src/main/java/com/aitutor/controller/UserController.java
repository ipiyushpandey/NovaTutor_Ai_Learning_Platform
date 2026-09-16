package com.aitutor.controller;

import com.aitutor.entity.User;
import com.aitutor.repository.UserRepository;
import com.aitutor.intelligence.LearningEvent;
import com.aitutor.intelligence.LearningEventRepository;
import com.aitutor.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LearningEventRepository learningEvents;

    public UserController(UserRepository users, PasswordEncoder encoder, JwtService jwt, LearningEventRepository learningEvents) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.learningEvents = learningEvents;
    }

    private User owner(Long pathId, Long headerId, String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                User tokenUser = users.findByEmail(jwt.email(authorization.substring(7))).orElseThrow();
                if (!pathId.equals(tokenUser.getId())) throw new IllegalArgumentException("You can only manage your own account");
                return tokenUser;
            } catch (IllegalArgumentException e) { throw e; } catch (Exception e) { throw new IllegalArgumentException("Invalid session"); }
        }
        if (headerId == null || !pathId.equals(headerId)) throw new IllegalArgumentException("You can only manage your own account");
        return users.findById(pathId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }


    /**
     * Public, privacy-safe leaderboard. Only real registered users with recorded
     * learning activity are returned; placeholder/demo names are never generated.
     */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard() {
        Map<Long, User> realUsers = users.findAll().stream()
                .collect(Collectors.toMap(User::getId, x -> x, (a,b) -> a));
        return learningEvents.leaderboardXp().stream()
                .filter(x -> x.getUserId() != null && x.getXp() > 0 && realUsers.containsKey(x.getUserId()))
                .map(x -> new LeaderboardEntry(x.getUserId(), realUsers.get(x.getUserId()).getName(), (int)Math.min(Integer.MAX_VALUE, x.getXp())))
                .toList();
    }

    public record LeaderboardEntry(Long id, String name, int score) {}

    @GetMapping("/{id}")
    public Profile get(@PathVariable Long id, @RequestHeader(value="X-User-Id", required=false) Long userId, @RequestHeader(value="Authorization", required=false) String authorization) {
        User u=owner(id,userId,authorization);
        return profile(u);
    }

    @PutMapping("/{id}")
    public Profile update(@PathVariable Long id, @RequestHeader(value="X-User-Id", required=false) Long userId, @RequestHeader(value="Authorization", required=false) String authorization, @RequestBody ProfileRequest r) {
        User u = owner(id, userId, authorization);
        if (r.name() == null || r.name().isBlank()) throw new IllegalArgumentException("Name is required");
        u.setName(r.name().trim());
        if(r.educationLevel()!=null) u.setEducationLevel(clean(r.educationLevel()));
        if(r.learningGoal()!=null) u.setLearningGoal(clean(r.learningGoal()));
        if(r.targetExam()!=null) u.setTargetExam(clean(r.targetExam()));
        if(r.interests()!=null) u.setInterests(clean(r.interests()));
        if(r.strengths()!=null) u.setStrengths(clean(r.strengths()));
        if(r.weaknesses()!=null) u.setWeaknesses(clean(r.weaknesses()));
        if(r.learningStyle()!=null) u.setLearningStyle(clean(r.learningStyle()));
        if(r.dailyStudyMinutes()!=null) u.setDailyStudyMinutes(r.dailyStudyMinutes());
        if(r.preferredAiLanguage()!=null) u.setPreferredAiLanguage(clean(r.preferredAiLanguage()));
        users.save(u);
        return profile(u);
    }

    @PostMapping("/{id}/password")
    public void changePassword(@PathVariable Long id, @RequestHeader(value="X-User-Id", required=false) Long userId, @RequestHeader(value="Authorization", required=false) String authorization, @RequestBody PasswordRequest r) {
        User u = owner(id, userId, authorization);
        if (r.currentPassword() == null || !encoder.matches(r.currentPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (r.newPassword() == null || r.newPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        u.setPassword(encoder.encode(r.newPassword()));
        users.save(u);
    }

    public record Profile(Long id, String name, String email, String role, String educationLevel, String learningGoal, String targetExam, String interests, String strengths, String weaknesses, String learningStyle, Integer dailyStudyMinutes, String preferredAiLanguage, boolean profileComplete) {}
    public record ProfileRequest(String name, String educationLevel, String learningGoal, String targetExam, String interests, String strengths, String weaknesses, String learningStyle, Integer dailyStudyMinutes, String preferredAiLanguage) {}
    public record PasswordRequest(String currentPassword, String newPassword) {}
    private String clean(String x){return x==null?null:x.trim();}
    private Profile profile(User u){boolean complete=u.getLearningGoal()!=null&&!u.getLearningGoal().isBlank()&&u.getInterests()!=null&&!u.getInterests().isBlank()&&u.getWeaknesses()!=null&&!u.getWeaknesses().isBlank()&&u.getLearningStyle()!=null&&!u.getLearningStyle().isBlank();return new Profile(u.getId(),u.getName(),u.getEmail(),u.getRole(),u.getEducationLevel(),u.getLearningGoal(),u.getTargetExam(),u.getInterests(),u.getStrengths(),u.getWeaknesses(),u.getLearningStyle(),u.getDailyStudyMinutes(),u.getPreferredAiLanguage(),complete);}
}
