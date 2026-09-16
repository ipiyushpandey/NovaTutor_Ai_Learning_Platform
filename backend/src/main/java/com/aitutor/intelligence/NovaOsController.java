package com.aitutor.intelligence;

import com.aitutor.repository.UserRepository;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nova-os")
public class NovaOsController {
    private final LearningEventRepository events;
    private final UserRepository users;
    private final AuthenticatedUser auth;

    public NovaOsController(LearningEventRepository events, UserRepository users, AuthenticatedUser auth) {
        this.events = events; this.users = users; this.auth = auth;
    }

    @GetMapping("/{userId}/overview")
    public Map<String,Object> overview(@PathVariable Long userId, Authentication authentication) {
        auth.requireSame(authentication, userId); users.findById(userId).orElseThrow();
        List<LearningEvent> all = events.findByUserIdOrderByCreatedAtAsc(userId);
        Map<String,Long> types = all.stream().collect(Collectors.groupingBy(LearningEvent::getType, Collectors.counting()));
        Map<String,List<LearningEvent>> concepts = all.stream().filter(e -> e.getTopic()!=null)
                .collect(Collectors.groupingBy(e -> e.getSubject()+"\u0000"+e.getTopic(), LinkedHashMap::new, Collectors.toList()));
        List<Map<String,Object>> weak = concepts.entrySet().stream().map(e -> {
            double avg=e.getValue().stream().filter(x->x.getScore()!=null).mapToInt(LearningEvent::getScore).average().orElse(0);
            String[] k=e.getKey().split("\u0000",2);
            return Map.<String,Object>of("subject",k[0],"topic",k.length>1?k[1]:"Core Concepts","mastery",Math.round(avg),"evidence",e.getValue().size());
        }).sorted(Comparator.comparingInt(x -> ((Number)x.get("mastery")).intValue())).limit(6).toList();
        List<Map<String,Object>> actions = new ArrayList<>();
        if (!weak.isEmpty()) {
            Map<String,Object> w=weak.get(0);
            actions.add(Map.of("module","AI Teacher","title","Recover "+w.get("topic"),"reason","Lowest recent concept signal","target","tutor"));
            actions.add(Map.of("module","Smart Quiz","title","Practice "+w.get("topic"),"reason","Convert the weak concept into retrieval practice","target","quiz"));
            actions.add(Map.of("module","Revision","title","Schedule a recovery review","reason","Protect the concept after practice","target","revision"));
        } else {
            actions.add(Map.of("module","Smart Quiz","title","Take your first diagnostic","reason","Nova needs evidence before it can personalize the path","target","quiz"));
        }
        return Map.of("events",all.size(),"eventTypes",types,"weakConcepts",weak,"nextActions",actions,
                "today",LocalDate.now(),"message","Nova connects practice, mistakes, revision and teaching into one learning loop.");
    }
}
