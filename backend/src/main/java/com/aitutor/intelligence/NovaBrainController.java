package com.aitutor.intelligence;

import com.aitutor.entity.User;
import com.aitutor.quiz.QuizQuestion;
import com.aitutor.quiz.QuizQuestionRepository;
import com.aitutor.repository.UserRepository;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/nova-brain")
public class NovaBrainController {
    private final LearningEventRepository events;
    private final QuizAttemptRepository attempts;
    private final QuizQuestionRepository questions;
    private final UserRepository users;
    private final AuthenticatedUser auth;
    private final LearningEventAnalyticsService analytics;

    public NovaBrainController(LearningEventRepository events, QuizAttemptRepository attempts,
                               QuizQuestionRepository questions, UserRepository users, AuthenticatedUser auth, LearningEventAnalyticsService analytics) {
        this.events = events; this.attempts = attempts; this.questions = questions; this.users = users; this.auth = auth; this.analytics = analytics;
    }

    public record Mastery(String subject, int mastery, int attempts, String trend, String state) {}
    public record Mistake(String id, String subject, String topic, String question, int repeats, String reason) {}
    public record Brain(String learnerName, int overall, int retention, int consistency, int persistence,
                        int studyMinutes, int streak, String nextAction, String nextReason,
                        String nextTarget, List<Mastery> mastery, List<Mistake> mistakes,
                        List<Map<String,Object>> recent, List<Map<String,Object>> heatmap) {}

    @GetMapping("/{userId}")
    @Transactional(readOnly = true)
    public Brain brain(@PathVariable Long userId, Authentication authentication) {
        auth.requireSame(authentication, userId);
        User user = users.findById(userId).orElseThrow();
        List<LearningEvent> recentEvents = events.findTop8ByUserIdOrderByCreatedAtDesc(userId);
        List<QuizAttempt> qs = attempts.findTop200ByUserIdOrderBySubmittedAtDesc(userId);
        var summary = analytics.summary(userId);
        var last28 = analytics.last28Days(userId);

        Map<String,List<QuizAttempt>> grouped = qs.stream().filter(q -> q.getCourse()!=null)
                .collect(Collectors.groupingBy(q -> q.getCourse().getTitle(), LinkedHashMap::new, Collectors.toList()));
        List<Mastery> mastery = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<QuizAttempt> list = entry.getValue();
            int total=list.stream().mapToInt(QuizAttempt::getTotal).sum();
            int correct=list.stream().mapToInt(QuizAttempt::getCorrect).sum();
            int score=total==0?0:(int)Math.round(correct*100.0/total);
            String trend="Stable";
            if(list.size()>=2){
                double n=list.get(0).getScore(), o=list.get(Math.min(3,list.size()-1)).getScore();
                trend=n>o+5?"Improving":n<o-5?"Dropping":"Stable";
            }
            String state=score>=85?"Mastered":score>=65?"Learning":score==0?"Not started":"Needs work";
            mastery.add(new Mastery(entry.getKey(),score,list.size(),trend,state));
        }
        mastery.sort(Comparator.comparingInt(Mastery::mastery));

        Map<String,Long> repeated = qs.stream().flatMap(q -> parseWrong(q).stream())
                .collect(Collectors.groupingBy(x->x,Collectors.counting()));
        List<Mistake> mistakes=new ArrayList<>();
        repeated.entrySet().stream().filter(e->e.getValue()>=1).sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(10).forEach(e->{
            try {
                QuizQuestion q=questions.findById(Long.valueOf(e.getKey())).orElse(null);
                if(q!=null) mistakes.add(new Mistake(e.getKey(),q.getCourse()==null?"":q.getCourse().getTitle(),
                        "Practice question",q.getQuestion(),e.getValue().intValue(),e.getValue()>=2?"Repeated miss — target this concept again":"Missed once — keep it in review"));
            } catch(Exception ignored) {}
        });

        int studyMinutes=(int)Math.min(Integer.MAX_VALUE,Math.round(summary.studySeconds()/60.0));
        int streak=analytics.streak(userId);
        int accuracy=qs.isEmpty()?0:(int)Math.round(qs.stream().mapToInt(QuizAttempt::getScore).average().orElse(0));
        long revisions=events.countByUserIdAndType(userId,"REVISION_COMPLETED");
        long adaptivePassed=events.countByUserIdAndTypeAndScoreGreaterThanEqual(userId,"ADAPTIVE_QUESTION",70);
        int retention=Math.min(100,(int)Math.round(Math.min(60,revisions*10)+Math.min(40,adaptivePassed*5)));
        int consistency=last28.values().stream().filter(a->a.count()>0).mapToInt(a->1).sum()*100/28;
        int persistence=Math.min(100,Math.max(0,(int)Math.round(Math.min(100,qs.size()*6 + summary.eventCount()*2))));
        int overall=qs.isEmpty()?Math.min(100,consistency/2):Math.round((accuracy+retention+consistency+persistence)/4f);

        String target="quiz";
        String action="Build your first learning signal";
        String reason="Complete a lesson or short practice set so Nova can personalize what comes next.";
        if(!mastery.isEmpty()){
            Mastery weak=mastery.get(0);
            if(weak.mastery()<65){ target="revision"; action="Fix "+weak.subject(); reason=weak.mastery()+"% recent mastery. Nova recommends a focused recovery session."; }
            else if(weak.mastery()<85){ target="quiz"; action="Strengthen "+weak.subject(); reason="Your understanding is growing. Use targeted retrieval before moving on."; }
            else { target="courses"; action="Continue your next lesson"; reason="Your recent quiz signals are strong. Keep building breadth."; }
        }
        List<Map<String,Object>> recent=recentEvents.stream()
                .map(e->Map.<String,Object>of("type",e.getType(),"subject",Objects.toString(e.getSubject(),""),"topic",Objects.toString(e.getTopic(),""),"score",Objects.toString(e.getScore(),""),"createdAt",e.getCreatedAt().toString())).toList();
        List<Map<String,Object>> heat=last28.entrySet().stream()
                .map(e->Map.<String,Object>of("date",e.getKey().toString(),"count",Math.min(4,e.getValue().count()),"studySeconds",e.getValue().studySeconds(),"xp",e.getValue().xp()))
                .toList();
        return new Brain(user.getName(),overall,retention,consistency,persistence,studyMinutes,streak,action,reason,target,mastery.stream().limit(12).toList(),mistakes,recent,heat);
    }

    private static List<String> parseWrong(QuizAttempt q){if(q.getWrongQuestionIds()==null||q.getWrongQuestionIds().isBlank())return List.of();return Arrays.stream(q.getWrongQuestionIds().split(",")).map(String::trim).filter(x->!x.isBlank()).toList();}
}
