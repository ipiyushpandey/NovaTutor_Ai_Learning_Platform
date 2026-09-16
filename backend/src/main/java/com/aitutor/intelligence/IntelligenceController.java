package com.aitutor.intelligence;

import com.aitutor.entity.Course;
import com.aitutor.entity.User;
import com.aitutor.repository.CourseRepository;
import com.aitutor.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {
 private final LearningEventRepository events; private final QuizAttemptRepository attempts; private final UserRepository users; private final AuthenticatedUser authenticatedUser; private final LearningEventService eventService; private final LearningEventAnalyticsService analytics;
 public IntelligenceController(LearningEventRepository e,QuizAttemptRepository a,UserRepository u, AuthenticatedUser au, LearningEventService eventService, LearningEventAnalyticsService analytics){events=e;attempts=a;users=u;authenticatedUser=au;this.eventService=eventService;this.analytics=analytics;}
 public record EventRequest(String type,String subject,String topic,Integer score,Integer durationSeconds,String details){}
 public record WeakArea(String subject,String topic,int attempts,int correct,int accuracy,String reason){}
 public record Recommendation(String type,String title,String description,String action,String target){ }
 public record Mastery(String subject,int attempts,int accuracy,String state,String trend){}
 public record MistakePattern(String label,int count,String detail){}
 public record MemoryView(String learnerName,int learningEvents,int quizzes,int lessons,int studyMinutes,int xp,int streak,List<WeakArea> weakAreas,List<Recommendation> recommendations,List<Map<String,Object>> recentActivity,List<Mastery> mastery,List<MistakePattern> mistakePatterns,Map<String,Integer> activityByDay){}
 @PostMapping("/{userId}/events")
 public Map<String,Object> event(@PathVariable Long userId,@RequestBody EventRequest req, Authentication authentication){
   authenticatedUser.requireSame(authentication,userId);
   User u=users.findById(userId).orElseThrow();
   if(req==null||req.type()==null||req.type().isBlank()) throw new IllegalArgumentException("Learning event type is required");
   String type=req.type().trim().toUpperCase(Locale.ROOT);
   if(!Set.of("DPP_COMPLETED","STUDY_SPRINT_COMPLETED","STUDY_SESSION_COMPLETED").contains(type)) throw new IllegalArgumentException("Unsupported client learning event type");
   String details=clean(req.details(),1000);
   int score=req.score()==null?0:Math.max(0,Math.min(100,req.score()));
   String dedupeKey=eventDedupeKey(type, u.getId(), req.subject(), req.topic(), details);
   eventService.record(u,type,clean(req.subject(),120),clean(req.topic(),180),score,safeDuration(req.durationSeconds()),details,dedupeKey);
   return Map.of("saved",true,"type",type);
 }
 @GetMapping("/{userId}") @Transactional(readOnly=true)
 public MemoryView memory(@PathVariable Long userId, Authentication authentication){
   authenticatedUser.requireSame(authentication,userId);
   User u=users.findById(userId).orElseThrow(); List<LearningEvent> es=events.findTop8ByUserIdOrderByCreatedAtDesc(userId);
   List<QuizAttempt> qs=attempts.findTop100ByUserIdOrderBySubmittedAtDesc(userId);
   var summary=analytics.summary(userId);
   int lessons=(int)Math.min(Integer.MAX_VALUE,summary.lessonCount());
   int minutes=(int)Math.min(Integer.MAX_VALUE,Math.round(summary.studySeconds()/60.0));
   int xp=(int)Math.min(Integer.MAX_VALUE,summary.xp());
   int streak=analytics.streak(userId);
   Map<String,List<QuizAttempt>> grouped=qs.stream().filter(q->q.getCourse()!=null).collect(Collectors.groupingBy(q->q.getCourse().getTitle(),LinkedHashMap::new,Collectors.toList()));
   List<WeakArea> weak=new ArrayList<>();
   for (Map.Entry<String,List<QuizAttempt>> entry : grouped.entrySet()) {
      String subject = entry.getKey();
      List<QuizAttempt> list = entry.getValue();
      int total = list.stream().mapToInt(QuizAttempt::getTotal).sum();
      int correct = list.stream().mapToInt(QuizAttempt::getCorrect).sum();
      int acc = total == 0 ? 0 : (int)Math.round(correct * 100.0 / total);
      if (acc < 75) {
        weak.add(new WeakArea(subject, "Quiz mastery", list.size(), correct, acc,
          acc < 50 ? "High priority: repeated quiz weakness" : "Needs another focused revision"));
      }
    }
   Map<String, Long> repeatedMistakes = qs.stream()
      .flatMap(q -> parseWrong(q).stream())
      .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
   int mistakeCount = 0;
   for (Map.Entry<String, Long> entry : repeatedMistakes.entrySet()) {
      if (entry.getValue() < 2 || mistakeCount >= 5) continue;
      weak.add(new WeakArea(
         "Mistake Lab",
         "Question #" + entry.getKey(),
         entry.getValue().intValue(),
         0,
         0,
         "This question has been missed repeatedly"
      ));
      mistakeCount++;
   }
   weak=weak.stream().sorted(Comparator.comparingInt(WeakArea::accuracy)).limit(6).toList();
   List<Mastery> mastery=new ArrayList<>();
   for (Map.Entry<String,List<QuizAttempt>> entry : grouped.entrySet()) {
      String subject=entry.getKey(); List<QuizAttempt> list=entry.getValue();
      int total=list.stream().mapToInt(QuizAttempt::getTotal).sum();
      int correct=list.stream().mapToInt(QuizAttempt::getCorrect).sum();
      int rawAcc=total==0?0:(int)Math.round(correct*100.0/total);
      double weighted=0, weight=0;
      for(int i=0;i<list.size();i++){ double w=Math.max(0.35,1.0-(i*0.12)); weighted+=(list.get(i).getScore())*w; weight+=w; }
      int acc=weight==0?rawAcc:(int)Math.round(Math.max(0,Math.min(100,weighted/weight)));
      String state=acc>=85?"Mastered":acc>=65?"Learning":"Needs work";
      List<QuizAttempt> recent=list.stream().limit(4).toList();
      String trend="Stable";
      if(recent.size()>=2){
        double newest=recent.get(0).getScore();
        double older=recent.get(recent.size()-1).getScore();
        trend=newest>older+5?"Improving":newest<older-5?"Dropping":"Stable";
      }
      mastery.add(new Mastery(subject,list.size(),acc,state,trend));
   }
   mastery=mastery.stream().sorted(Comparator.comparingInt(Mastery::accuracy)).limit(8).toList();
   List<MistakePattern> mistakePatterns=repeatedMistakes.entrySet().stream()
      .filter(e->e.getValue()>=2).sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(6)
      .map(e->new MistakePattern("Question #"+e.getKey(),e.getValue().intValue(),"Repeated misses suggest this concept needs a targeted retry."))
      .toList();
   List<Recommendation> rec=new ArrayList<>();
   if(!weak.isEmpty()){WeakArea w=weak.get(0);rec.add(new Recommendation("WEAKNESS","Fix your weakest area","Spend 15 minutes revising "+w.subject+" before attempting another test.","Open Analytics","analytics"));}
   if(!qs.isEmpty()){QuizAttempt last=qs.get(0);if(last.getScore()<80)rec.add(new Recommendation("RETRY","Review your last quiz","You scored "+last.getScore()+"%. Revisit the missed concepts, then retry.","Open Smart Quiz","quiz"));}
   if(lessons>0)rec.add(new Recommendation("REVISION","Keep your memory fresh","Turn completed lessons into short revision sessions instead of rereading everything.","Open Flashcards","flashcards"));
   if(rec.size()<3)rec.add(new Recommendation("NEXT_STEP","Build today's momentum","Complete one lesson or five practice questions today to keep your learning streak alive.","Continue learning","courses"));
   List<Map<String,Object>> recent=es.stream().limit(8).map(e->Map.<String,Object>of("type",e.getType(),"subject",Objects.toString(e.getSubject(),""),"topic",Objects.toString(e.getTopic(),""),"score",Objects.toString(e.getScore(),""),"createdAt",e.getCreatedAt().toString())).toList();
   Map<String,Integer> activityByDay=new LinkedHashMap<>();
   analytics.last28Days(userId).forEach((date,activity)->activityByDay.put(date.toString(),Math.min(4,(int)activity.count())));
   return new MemoryView(u.getName(),(int)Math.min(Integer.MAX_VALUE,summary.eventCount()),qs.size(),lessons,minutes,xp,streak,weak,rec,recent,mastery,mistakePatterns,activityByDay);
 }
 private static String eventDedupeKey(String type,Long userId,String subject,String topic,String details){
   if(type==null)return null;
   String t=type.toUpperCase(Locale.ROOT);
   if(Set.of("LESSON_COMPLETED").contains(t)) return t+":"+userId+":"+clean(subject,120)+":"+clean(topic,180);
   if(Set.of("DPP_COMPLETED","REVISION_COMPLETED").contains(t)) return t+":"+userId+":"+java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))+":"+clean(subject,120)+":"+clean(topic,180);
   return null;
 }
 private static String clean(String s,int max){if(s==null)return null;s=s.trim();return s.isBlank()?null:s.substring(0,Math.min(max,s.length()));}
 private static int safeDuration(Integer x){return x==null?0:Math.max(0,Math.min(86400,x));}
 private static List<String> parseWrong(QuizAttempt q){if(q.getWrongQuestionIds()==null||q.getWrongQuestionIds().isBlank())return List.of();return Arrays.stream(q.getWrongQuestionIds().split(",")).map(String::trim).filter(x->!x.isBlank()).toList();}
}
