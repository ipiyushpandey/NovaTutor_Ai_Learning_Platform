package com.aitutor.controller;

import com.aitutor.ai.*;
import com.aitutor.entity.Course;
import com.aitutor.entity.User;
import com.aitutor.repository.CourseRepository;
import com.aitutor.repository.UserRepository;
import com.aitutor.service.AiService;
import com.aitutor.intelligence.LearningEvent;
import com.aitutor.intelligence.LearningEventRepository;
import com.aitutor.intelligence.LearningEventService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;
import java.util.*;

@RestController
@RequestMapping("/api/dpp")
public class DppController {
 private final AiService ai; private final CourseRepository courses; private final UserRepository users; private final AuthenticatedUser authenticatedUser; private final LearningEventRepository events; private final LearningEventService eventService;
 public DppController(AiService ai,CourseRepository courses,UserRepository users,AuthenticatedUser authenticatedUser,LearningEventRepository events,LearningEventService eventService){this.ai=ai;this.courses=courses;this.users=users;this.authenticatedUser=authenticatedUser;this.events=events;this.eventService=eventService;}
 @PostMapping("/generate")
 public AiResponse generate(@RequestBody Request r, Authentication authentication){
   Course c=courses.findById(r.courseId()).orElseThrow(); User u=authenticatedUser.require(authentication);
   String prompt="Create today's Daily Practice Problems (DPP) for the student. Course: " + c.getTitle()
     + ". Description: " + (c.getDescription()==null?"":c.getDescription())
     + ". Student goal: " + safe(u.getLearningGoal())
     + ". Weaknesses: " + safe(u.getWeaknesses())
     + ". Interests: " + safe(u.getInterests())
     + ". Learning style: " + safe(u.getLearningStyle())
     + ". Generation seed: " + String.valueOf(r.requestSeed()==null?System.currentTimeMillis():r.requestSeed())
     + ". Avoid repeating these recent DPPs:\n" + recentDppExcerpts(u.getId(), c.getId())
     + "\nReturn ONLY valid JSON (no markdown fences) with this exact shape: {\"title\":\"...\",\"tasks\":[{\"number\":1,\"type\":\"MCQ\",\"question\":\"...\",\"options\":[{\"label\":\"A\",\"text\":\"...\"},{\"label\":\"B\",\"text\":\"...\"},{\"label\":\"C\",\"text\":\"...\"},{\"label\":\"D\",\"text\":\"...\"}],\"answer\":\"A\",\"explanation\":\"...\"}]. Use exactly 6 tasks: 3 MCQs, 2 practical problems, and 1 challenge. Practical/challenge tasks must use type PRACTICAL and options as an empty array; their answer should be a concise expected answer or key points, not a full solution. For coding courses include at least 2 coding problems. Do not reveal answers outside each task's answer field. Keep it course-relevant and useful for exams/interviews. Language: " + safe(u.getPreferredAiLanguage());
   AiResponse response=ai.ask(new AiRequest(AiTask.TUTOR,c.getTitle(),prompt,"intermediate",safe(u.getPreferredAiLanguage())));
   String seed=String.valueOf(r.requestSeed()==null?System.currentTimeMillis():r.requestSeed());
   String answer=response==null?null:response.answer();
   String fingerprint=fingerprint(answer);
   String excerpt=answer==null?"":answer.replaceAll("\\s+"," ").trim();
   if(excerpt.length()>760) excerpt=excerpt.substring(0,760);
   eventService.record(u,"DPP_GENERATED",c.getTitle(),"Daily Practice Problems",null,0,"seed="+seed+";fingerprint="+fingerprint+";content="+excerpt,"DPP_GENERATED:"+u.getId()+":"+c.getId()+":"+seed);
   return response;
 }
 private String recentDppExcerpts(Long userId,Long courseId){
   String courseTitle=courses.findById(courseId).map(Course::getTitle).orElse("");
   return events.findTop5ByUserIdAndTypeOrderByCreatedAtDesc(userId,"DPP_GENERATED").stream()
      .filter(e->Objects.equals(e.getSubject(),courseTitle))
      .map(LearningEvent::getDetails).reduce((a,b)->a+"\n"+b).orElse("none");
 }
 private String fingerprint(String answer){if(answer==null)return "none";String normalized=answer.replaceAll("\\s+"," ").trim();return Integer.toHexString(normalized.hashCode());}
 private String safe(String x){return x==null||x.isBlank()?"not specified":x;}
 public record Request(Long userId,Long courseId,Long requestSeed){}
}
