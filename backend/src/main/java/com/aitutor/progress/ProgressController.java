package com.aitutor.progress;

import com.aitutor.entity.*;
import com.aitutor.repository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;
import java.util.*;
import com.aitutor.intelligence.LearningEvent;
import com.aitutor.intelligence.LearningEventRepository;

@RestController @RequestMapping("/api/progress")
public class ProgressController {
    private final ProgressRepository pr; private final UserRepository ur; private final CourseRepository cr; private final LessonRepository lr; private final LearningEventRepository events; private final com.aitutor.intelligence.LearningEventService eventService; private final AuthenticatedUser authenticatedUser;
    public ProgressController(ProgressRepository pr,UserRepository ur,CourseRepository cr,LessonRepository lr,LearningEventRepository events, com.aitutor.intelligence.LearningEventService eventService, AuthenticatedUser authenticatedUser){this.pr=pr;this.ur=ur;this.cr=cr;this.lr=lr;this.events=events;this.eventService=eventService;this.authenticatedUser=authenticatedUser;}
    record View(Long courseId,int completedLessons,int totalLessons,double percent){}
    @GetMapping("/{userId}") List<View> all(@PathVariable Long userId, Authentication authentication){
        authenticatedUser.requireSame(authentication,userId);
        ur.findById(userId).orElseThrow(); return pr.findByUserId(userId).stream().map(p->new View(p.getCourse().getId(),p.getCompletedLessons(),p.getTotalLessons(),p.getPercent())).toList();
    }
    @PostMapping("/{userId}/course/{courseId}/lesson/{lessonId}/complete") View complete(@PathVariable Long userId,@PathVariable Long courseId,@PathVariable Long lessonId, Authentication authentication){
        authenticatedUser.requireSame(authentication,userId);
        User u=ur.findById(userId).orElseThrow();
        Course c=cr.findById(courseId).orElseThrow();
        Lesson lesson=lr.findById(lessonId).orElseThrow();
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Lesson does not belong to this course");
        }
        Progress p=pr.findByUserIdAndCourseId(userId,courseId).orElseGet(()->{Progress x=new Progress();x.setUser(u);x.setCourse(c);x.setTotalLessons(c.getTotalLessons());return x;});
        int total=Math.max(1,c.getTotalLessons());
        int completed=p.getCompletedLessons();
        // orderIndex can be topic-local in older seeded curricula. Always derive the
        // authoritative course sequence from the actual ordered lesson list so the
        // My Learning UI and completion state stay in sync.
        // orderIndex is topic-local in older seeded curricula, so a plain
        // ORDER BY orderIndex can interleave topics unpredictably. Build the
        // same deterministic sequence used by My Learning: first-seen topic
        // order, then topic-local orderIndex, then lesson id as a tie-breaker.
        List<Lesson> raw=lr.findByCourseIdOrderById(courseId);
        LinkedHashMap<String,List<Lesson>> groups=new LinkedHashMap<>();
        for(Lesson l:raw){
            String topic=l.getTopic();
            if(topic==null||topic.isBlank()) topic=String.valueOf(l.getTitle());
            groups.computeIfAbsent(topic,k->new ArrayList<>()).add(l);
        }
        List<Lesson> ordered=new ArrayList<>();
        for(List<Lesson> group:groups.values()){
            group.sort(Comparator.comparing((Lesson l)->l.getOrderIndex()==null?Integer.MAX_VALUE:l.getOrderIndex())
                    .thenComparing(Lesson::getId));
            ordered.addAll(group);
        }
        int order=completed+1;
        p.setCompletedLessons(Math.min(total,completed+1)); p.setTotalLessons(total); p.setPercent(p.getCompletedLessons()*100.0/total); pr.save(p); eventService.record(u,"LESSON_COMPLETED",c.getTitle(),lesson.getTitle(),100,0,"Completed lesson #"+order,"LESSON_COMPLETED:"+u.getId()+":"+c.getId()+":"+lesson.getId());
        return new View(courseId,p.getCompletedLessons(),p.getTotalLessons(),p.getPercent());
    }
}
