package com.aitutor.intelligence;

import com.aitutor.entity.User;
import com.aitutor.repository.UserRepository;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/intelligence")
public class DeepIntelligenceController {
    private final LearningEventRepository events;
    private final UserRepository users;
    private final AuthenticatedUser authenticatedUser;

    public DeepIntelligenceController(LearningEventRepository events, UserRepository users, AuthenticatedUser authenticatedUser) {
        this.events=events; this.users=users; this.authenticatedUser=authenticatedUser;
    }

    public record Dna(int overall, int accuracy, int consistency, int retention, int persistence, int breadth, String profile, String explanation) {}
    public record Concept(String subject, String topic, int mastery, int evidence, String state, String trend) {}
    public record Edge(String from, String to, String relation) {}
    public record Graph(List<String> nodes, List<Edge> edges) {}
    public record DeepView(Dna dna, List<Concept> concepts, Graph graph, List<Map<String,Object>> mistakeDna) {}

    @GetMapping("/{userId}/deep")
    @Transactional(readOnly=true)
    public DeepView deep(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication,userId);
        User user=users.findById(userId).orElseThrow();
        List<LearningEvent> all=events.findByUserIdOrderByCreatedAtAsc(userId);
        List<LearningEvent> questionEvents=all.stream().filter(e->"QUIZ_QUESTION".equals(e.getType())).toList();
        List<LearningEvent> meaningful=all.stream().filter(e->!"QUIZ_QUESTION".equals(e.getType())).toList();

        int accuracy=questionEvents.isEmpty()?0:(int)Math.round(questionEvents.stream().mapToInt(e->e.getScore()==null?0:e.getScore()).average().orElse(0));
        Map<LocalDate,Integer> activeDays=meaningful.stream().collect(Collectors.groupingBy(e->e.getCreatedAt().toLocalDate(),Collectors.summingInt(e->1)));
        int consistency=consistency(activeDays);
        int retention=retention(questionEvents);
        int persistence=persistence(all);
        int breadth=(int)Math.min(100,Math.round(Math.min(1.0,meaningful.stream().map(LearningEvent::getSubject).filter(Objects::nonNull).distinct().count()/8.0)*100));
        int overall=(int)Math.round(accuracy*.30+consistency*.20+retention*.20+persistence*.15+breadth*.15);
        String profile=profile(accuracy,consistency,retention,persistence,breadth);
        String explanation="Nova combines accuracy, consistency, retention, persistence and learning breadth instead of treating one quiz score as your identity.";
        Dna dna=new Dna(overall,accuracy,consistency,retention,persistence,breadth,profile,explanation);

        Map<String,List<LearningEvent>> grouped=questionEvents.stream().collect(Collectors.groupingBy(e->key(e.getSubject(),e.getTopic()),LinkedHashMap::new,Collectors.toList()));
        List<Concept> concepts=new ArrayList<>();
        for(var entry:grouped.entrySet()){
            List<LearningEvent> xs=entry.getValue();
            int acc=(int)Math.round(xs.stream().mapToInt(e->e.getScore()==null?0:e.getScore()).average().orElse(0));
            String[] parts=entry.getKey().split("\\u0000",2); String subject=parts[0], topic=parts.length>1?parts[1]:"Core Concepts";
            String state=acc>=85?"MASTERED":acc>=65?"LEARNING":"NEEDS_WORK";
            List<LearningEvent> recent=xs.subList(Math.max(0,xs.size()-Math.min(4,xs.size())),xs.size());
            double oldAvg=xs.stream().limit(Math.max(1,xs.size()-Math.min(4,xs.size()))).mapToInt(e->e.getScore()==null?0:e.getScore()).average().orElse(acc);
            double recentAvg=recent.stream().mapToInt(e->e.getScore()==null?0:e.getScore()).average().orElse(acc);
            String trend=recentAvg>oldAvg+5?"IMPROVING":recentAvg<oldAvg-5?"DROPPING":"STABLE";
            concepts.add(new Concept(subject,topic,acc,xs.size(),state,trend));
        }
        concepts.sort(Comparator.comparingInt(Concept::mastery));
        if(concepts.size()>30) concepts=new ArrayList<>(concepts.subList(0,30));

        LinkedHashSet<String> nodes=new LinkedHashSet<>(); List<Edge> edges=new ArrayList<>();
        for(Concept c:concepts) nodes.add(c.topic());
        List<Concept> bySubject=new ArrayList<>(concepts);
        Map<String,List<Concept>> bySub=bySubject.stream().collect(Collectors.groupingBy(Concept::subject,LinkedHashMap::new,Collectors.toList()));
        for(List<Concept> list:bySub.values()){
            list.sort(Comparator.comparingInt(Concept::mastery));
            for(int i=0;i+1<list.size() && edges.size()<40;i++) edges.add(new Edge(list.get(i).topic(),list.get(i+1).topic(),"next learning layer"));
        }
        Graph graph=new Graph(new ArrayList<>(nodes),edges);

        Map<String,List<LearningEvent>> mistakes=questionEvents.stream().filter(e->Objects.equals(e.getScore(),0)).collect(Collectors.groupingBy(e->key(e.getSubject(),e.getTopic()),LinkedHashMap::new,Collectors.toList()));
        List<Map<String,Object>> mistakeDna=mistakes.entrySet().stream().sorted((a,b)->Integer.compare(b.getValue().size(),a.getValue().size())).limit(8).map(e->{
            String[] p=e.getKey().split("\\u0000",2); int n=e.getValue().size();
            String type=n>=4?"REPEATED_CONCEPT":n>=2?"RECURRING_MISS":"SINGLE_MISS";
            return Map.<String,Object>of("subject",p[0],"topic",p.length>1?p[1]:"Core Concepts","count",n,"type",type,"signal",n>=3?"Needs targeted recovery":"Keep this concept in the next revision cycle");
        }).toList();
        return new DeepView(dna,concepts,graph,mistakeDna);
    }

    private static String key(String s,String t){return Objects.toString(s,"Unknown")+"\u0000"+Objects.toString(t,"Core Concepts");}
    private static int consistency(Map<LocalDate,Integer> days){if(days.isEmpty())return 0; LocalDate end=LocalDate.now(LearningEventAnalyticsService.LEARNING_ZONE); int active=0; for(int i=0;i<28;i++)if(days.containsKey(end.minusDays(i)))active++; return (int)Math.round(active/28.0*100);}
    private static int retention(List<LearningEvent> q){if(q.isEmpty())return 0; Map<String,List<LearningEvent>> m=q.stream().collect(Collectors.groupingBy(e->key(e.getSubject(),e.getTopic()))); int repeated=0,improved=0; for(List<LearningEvent> xs:m.values()){if(xs.size()<2)continue; repeated++; int first=xs.get(0).getScore()==null?0:xs.get(0).getScore(); int last=xs.get(xs.size()-1).getScore()==null?0:xs.get(xs.size()-1).getScore(); if(last>=first)improved++;} return repeated==0?Math.min(70,(int)Math.round(q.stream().mapToInt(e->e.getScore()==null?0:e.getScore()).average().orElse(0))):(int)Math.round(improved*100.0/repeated);}
    private static int persistence(List<LearningEvent> all){if(all.isEmpty())return 0; long practice=all.stream().filter(e->Set.of("QUIZ_QUESTION","LESSON_COMPLETED","REVISION_COMPLETED","TEST_COMPLETED","EXAM_COMPLETED").contains(e.getType())).count(); return (int)Math.min(100,Math.round(practice/20.0*100));}
    private static String profile(int a,int c,int r,int p,int b){if(a>=80&&c>=70)return "Consistent Mastery Builder";if(p>=70&&a<70)return "Persistent Improver";if(r>=75)return "Strong Retention Learner";if(b>=75)return "Broad Explorer";return "Developing Learner";}
}
