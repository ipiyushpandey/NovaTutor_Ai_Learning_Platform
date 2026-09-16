package com.aitutor.intelligence;

import com.aitutor.entity.Course;
import com.aitutor.entity.User;
import com.aitutor.quiz.QuizQuestion;
import com.aitutor.quiz.QuizQuestionRepository;
import com.aitutor.ai.AiProvider;
import com.aitutor.ai.AiRequest;
import com.aitutor.ai.AiTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aitutor.repository.CourseRepository;
import com.aitutor.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/adaptive")
public class AdaptiveController {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LearningEventRepository events;
    private final LearningEventService eventService;
    private final QuizAttemptRepository attempts;
    private final QuizQuestionRepository questions;
    private final AuthenticatedUser authenticatedUser;
    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

    public AdaptiveController(UserRepository users, CourseRepository courses, LearningEventRepository events,
                              QuizAttemptRepository attempts, QuizQuestionRepository questions, AuthenticatedUser authenticatedUser,
                              AiProvider aiProvider, LearningEventService eventService) {
        this.users=users; this.courses=courses; this.events=events; this.attempts=attempts; this.questions=questions; this.authenticatedUser=authenticatedUser;
        this.aiProvider=aiProvider;
        this.objectMapper=new ObjectMapper();
        this.eventService=eventService;
    }

    public record Question(Long id, String course, String text, List<String> options, String difficulty, String topic) {}
    public record ScoreCard(int readiness, int confidence, int mastered, int needsWork, int dueRevision, String headline, List<Map<String,Object>> subjects) {}
    public record Revision(String subject, String topic, String reason, String dueLabel, int priority, String interval, String nextReview) {}
    public record Concept(String subject, String topic, int mastery, String state) {}
    public record AdaptiveView(ScoreCard readiness, List<Question> questions, List<Revision> revisions, List<Concept> map, List<String> rescuePlan, String adaptiveLevel, String adaptiveReason) {}

    @GetMapping("/{userId}")
    @Transactional(readOnly=true)
    public AdaptiveView view(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication,userId);
        User u=users.findById(userId).orElseThrow();
        List<QuizAttempt> qs=attempts.findTop200ByUserIdOrderBySubmittedAtDesc(userId);
        List<LearningEvent> es=events.findTop200ByUserIdOrderByCreatedAtDesc(userId);
        Map<String,List<QuizAttempt>> bySubject=qs.stream().filter(q->q.getCourse()!=null)
                .collect(Collectors.groupingBy(q->q.getCourse().getTitle(),LinkedHashMap::new,Collectors.toList()));
        Set<Long> recentlySeen=new LinkedHashSet<>();
        for(LearningEvent e:es){
            if(e.getUser()!=null && "ADAPTIVE_SHOWN".equals(e.getType()) && e.getDetails()!=null){
                String d=e.getDetails(); if(d.startsWith("questionId=")){ try{ recentlySeen.add(Long.valueOf(d.substring(11))); }catch(Exception ignored){} }
            }
            if(recentlySeen.size()>=40) break;
        }
        List<Map<String,Object>> subjects=new ArrayList<>();
        List<Revision> revisions=new ArrayList<>();
        List<Concept> map=new ArrayList<>();
        int mastered=0, needs=0, dueCount=0;
        for(Map.Entry<String,List<QuizAttempt>> e:bySubject.entrySet()) {
            int total=e.getValue().stream().mapToInt(QuizAttempt::getTotal).sum();
            int correct=e.getValue().stream().mapToInt(QuizAttempt::getCorrect).sum();
            int acc=total==0?0:(int)Math.round(correct*100.0/total);
            String state=acc>=80?"MASTERED":acc>=60?"LEARNING":"WEAK";
            if(acc>=80) mastered++; else needs++;
            subjects.add(Map.of("subject",e.getKey(),"accuracy",acc,"attempts",e.getValue().size(),"state",state));
            map.add(new Concept(e.getKey(),"Quiz mastery",acc,state));
            QuizAttempt last=e.getValue().get(0);
            LocalDateTime lastLearning=last.getSubmittedAt();
            LocalDateTime lastRevision=latestRevision(es,e.getKey());
            LocalDateTime base=lastLearning;
            if(lastRevision!=null && lastRevision.isAfter(base)) base=lastRevision;
            long days=Duration.between(base,LocalDateTime.now()).toDays();
            int interval=reviewInterval(acc, days);
            LocalDateTime next=base.plusDays(interval);
            boolean due=!next.isAfter(LocalDateTime.now());
            if(due) dueCount++;
            String label=due?"Due now":(interval<=1?"Due today":"Next review in "+interval+" days");
            String reason=due?(acc<60?"Repeated mistakes need attention":"Memory refresh is due"):(acc<60?"Weak area — keep it in your rotation":(acc<80?"Strengthen this before it fades":"Keep this concept active with a quick recall"));
            revisions.add(new Revision(e.getKey(),"Quiz mastery",reason,label,due?(acc<60?3:2):1,interval+" day"+(interval==1?"":"s"),next.toLocalDate().toString()));
        }
        for(Course c:courses.findAll()) if(!bySubject.containsKey(c.getTitle())) map.add(new Concept(c.getTitle(),"Course foundations",0,"NOT_STARTED"));
        revisions.sort(Comparator.comparingInt(Revision::priority).reversed().thenComparing(Revision::nextReview));
        int readiness=qs.isEmpty()?0:(int)Math.round(qs.stream().mapToInt(QuizAttempt::getScore).average().orElse(0));
        int confidence=Math.min(100, Math.max(0, readiness + Math.min(15, qs.size())));
        String headline=qs.isEmpty()?"Start a diagnostic quiz to build your readiness profile":readiness>=80?"You're building strong exam readiness":"Your next score can improve fastest by fixing the weakest areas";
        String adaptiveLevel=adaptiveLevel(qs);
        String adaptiveReason=adaptiveReason(adaptiveLevel);
        List<String> rescue=new ArrayList<>();
        revisions.stream().limit(5).forEach(r->rescue.add("20 min — " + r.subject()+": revise " + r.topic()+" and solve 5 focused questions."));
        if(rescue.isEmpty()) rescue.add("Take a 10-question diagnostic quiz, then NovaTutor will build a personalized recovery plan.");
        ScoreCard card=new ScoreCard(readiness,confidence,mastered,needs,dueCount,headline,subjects);
        List<Question> adaptive=adaptiveQuestions(qs,bySubject,adaptiveLevel,recentlySeen);
        return new AdaptiveView(card,adaptive,revisions.stream().limit(12).toList(),map.stream().limit(60).toList(),rescue,adaptiveLevel,adaptiveReason);
    }

    @PostMapping("/{userId}/ai-question")
    @Transactional
    public Question aiQuestion(@PathVariable Long userId, Authentication authentication) {
        authenticatedUser.requireSame(authentication,userId);
        User u=users.findById(userId).orElseThrow();
        List<QuizAttempt> recent=attempts.findTop200ByUserIdOrderBySubmittedAtDesc(userId).stream().limit(50).toList();
        String subject=recent.stream().filter(a->a.getCourse()!=null).map(a->a.getCourse().getTitle()).findFirst()
                .orElseGet(()->courses.findAll().stream().map(Course::getTitle).findFirst().orElse("Computer Science"));
        String level=adaptiveLevel(recent);
        String prompt="Generate ONE original, non-trivial exam-style MCQ for a computer-science learner. Subject: "+subject+". Difficulty: "+level+". "
                +"Use a reasoning/scenario/output-analysis style question rather than trivial recall. It may involve code, algorithms, data structures, systems or a trace. "
                +"Return ONLY valid JSON with keys question, options (array of exactly 4 strings), correctOption (0-3), explanation, difficulty, topic. "
                +"Do not copy known exam wording. Make exactly one option correct.";
        if(!aiProvider.available()) throw new IllegalStateException("AI question generation is unavailable until the Gemini API key is configured.");
        String responseLanguage = u.getPreferredAiLanguage() == null || u.getPreferredAiLanguage().isBlank()
                ? "English" : u.getPreferredAiLanguage();
        String raw=aiProvider.generate(new AiRequest(AiTask.QUIZ,subject,prompt,level.toLowerCase(Locale.ROOT),responseLanguage)).answer();
        JsonNode node=parseJson(raw);
        if(node==null || !node.hasNonNull("question") || !node.has("options") || !node.get("options").isArray() || node.get("options").size()!=4)
            throw new IllegalStateException("AI returned an invalid quiz question. Please try again.");
        String text=node.get("question").asText().trim();
        List<String> opts=new ArrayList<>(); node.get("options").forEach(x->opts.add(x.asText()));
        int correct=node.has("correctOption")?node.get("correctOption").asInt(-1):-1;
        if(correct<0||correct>3) throw new IllegalStateException("AI returned an invalid answer index.");
        QuizQuestion q=new QuizQuestion();
        Course course=courses.findAll().stream().filter(c->Objects.equals(c.getTitle(),subject)).findFirst().orElse(null);
        q.setCourse(course); q.setQuestion(text); q.setOptionA(opts.get(0)); q.setOptionB(opts.get(1)); q.setOptionC(opts.get(2)); q.setOptionD(opts.get(3));
        q.setCorrectOption(correct); q.setExplanation(node.path("explanation").asText("Review the reasoning behind the correct option."));
        q.setDifficulty(node.path("difficulty").asText(level)); q.setTopic(node.path("topic").asText(subject));
        if(course!=null) questions.save(q);
        if(course!=null) events.save(new LearningEvent(u,"ADAPTIVE_AI_GENERATED",subject,q.getTopic(),null,0,"questionId="+q.getId()));
        return new Question(q.getId(),subject,q.getQuestion(),List.of(q.getOptionA(),q.getOptionB(),q.getOptionC(),q.getOptionD()),q.getDifficulty(),q.getTopic());
    }

    private JsonNode parseJson(String raw) {
        if(raw==null) return null;
        String s=raw.trim();
        int start=s.indexOf('{'), end=s.lastIndexOf('}');
        if(start<0||end<=start) return null;
        try{return objectMapper.readTree(s.substring(start,end+1));}catch(Exception e){return null;}
    }

    @PostMapping("/{userId}/shown")
    @Transactional
    public Map<String,Object> shown(@PathVariable Long userId,@RequestBody Map<String,Object> body, Authentication authentication){
        authenticatedUser.requireSame(authentication,userId);
        User u=users.findById(userId).orElseThrow();
        Object ids=body.get("questionIds");
        if(ids instanceof List<?> list){
            for(Object id:list){
                try{
                    Long qid=Long.valueOf(String.valueOf(id));
                    QuizQuestion q=questions.findById(qid).orElse(null);
                    if(q!=null) events.save(new LearningEvent(u,"ADAPTIVE_SHOWN",q.getCourse()==null?null:q.getCourse().getTitle(),"Adaptive practice",null,0,"questionId="+qid));
                }catch(Exception ignored){}
            }
        }
        return Map.of("success",true);
    }

    @PostMapping("/{userId}/revision-complete")
    @Transactional
    public Map<String,Object> revisionComplete(@PathVariable Long userId,@RequestBody Map<String,Object> body, Authentication authentication){
        authenticatedUser.requireSame(authentication,userId);
        User u=users.findById(userId).orElseThrow();
        String subject=String.valueOf(body.getOrDefault("subject",""));
        String topic=String.valueOf(body.getOrDefault("topic","Quiz mastery"));
        eventService.record(u,"REVISION_COMPLETED",subject,topic,100,0,"Student marked this revision as reviewed.","REVISION_COMPLETED:"+u.getId()+":"+subject+":"+topic);
        int accuracy=0;
        List<QuizAttempt> subjectAttempts=attempts.findTop200ByUserIdOrderBySubmittedAtDesc(userId).stream()
                .filter(a->a.getCourse()!=null && Objects.equals(a.getCourse().getTitle(),subject)).toList();
        if(!subjectAttempts.isEmpty()){
            int total=subjectAttempts.stream().mapToInt(QuizAttempt::getTotal).sum();
            int correct=subjectAttempts.stream().mapToInt(QuizAttempt::getCorrect).sum();
            accuracy=total==0?0:(int)Math.round(correct*100.0/total);
        }
        int interval=reviewInterval(accuracy,0);
        String nextReview=LocalDateTime.now().plusDays(interval).toLocalDate().toString();
        return Map.of("success",true,"message","Revision recorded. Nova will schedule the next review from this session.","interval",interval+" day"+(interval==1?"":"s"),"nextReview",nextReview);
    }

    @PostMapping("/{userId}/answer")
    @Transactional
    public Map<String,Object> answer(@PathVariable Long userId,@RequestBody Map<String,Object> body, Authentication authentication){
        authenticatedUser.requireSame(authentication,userId);
        User u=users.findById(userId).orElseThrow();
        Long qid=Long.valueOf(String.valueOf(body.get("questionId")));
        int selected=Integer.parseInt(String.valueOf(body.get("selectedOption")));
        QuizQuestion q=questions.findById(qid).orElseThrow();
        boolean correct=selected==q.getCorrectOption();
        Course c=q.getCourse();
        eventService.record(u,"ADAPTIVE_QUESTION",c==null?null:c.getTitle(),"Adaptive practice",correct?100:0,0,"questionId="+qid+";"+(correct?"Correct":"Incorrect"),"ADAPTIVE_QUESTION:"+u.getId()+":"+qid+":"+System.currentTimeMillis()/60000);
        return Map.of("correct",correct,"correctOption",q.getCorrectOption(),"message",correct?"Correct. Difficulty can increase.":"Not quite. This concept should be revised before the next attempt.");
    }

    private List<Question> adaptiveQuestions(List<QuizAttempt> qs, Map<String,List<QuizAttempt>> bySubject, String level, Set<Long> recentlySeen){
        Set<Long> wrong=new HashSet<>();
        qs.stream().flatMap(q->parseWrong(q).stream()).forEach(x->{try{wrong.add(Long.valueOf(x));}catch(Exception ignored){}});
        List<QuizQuestion> pool=new ArrayList<>(questions.findAll());
        LinkedHashMap<Long,QuizQuestion> unique=new LinkedHashMap<>(); for(QuizQuestion q:pool) unique.putIfAbsent(q.getId(),q);
        List<QuizQuestion> fresh=new ArrayList<>();
        List<QuizQuestion> review=new ArrayList<>();
        int target=difficultyRank(level);
        for(QuizQuestion q:unique.values()){
            if(recentlySeen.contains(q.getId())) continue;
            if(wrong.contains(q.getId())) review.add(q); else fresh.add(q);
        }
        Comparator<QuizQuestion> byDifficulty=Comparator.comparingInt(q->Math.abs(difficultyRank(q.getDifficulty())-target));
        fresh.sort(byDifficulty); review.sort(byDifficulty);
        Collections.shuffle(fresh, new Random());
        Collections.shuffle(review, new Random());
        List<QuizQuestion> chosen=new ArrayList<>();
        // Keep targeted mistakes, but always mix in unseen/new questions.
        for(QuizQuestion q:review) if(chosen.size()<3) chosen.add(q);
        for(QuizQuestion q:fresh) if(chosen.size()<12) chosen.add(q);
        if(chosen.size()<12) for(QuizQuestion q:review) if(!chosen.contains(q)&&chosen.size()<12) chosen.add(q);
        return chosen.stream().map(q->new Question(q.getId(),q.getCourse()==null?"":q.getCourse().getTitle(),q.getQuestion(),List.of(q.getOptionA(),q.getOptionB(),q.getOptionC(),q.getOptionD()),q.getDifficulty(),wrong.contains(q.getId())?"Targeted review":"New question")).toList();
    }

    private static LocalDateTime latestRevision(List<LearningEvent> events,String subject){
        for(LearningEvent e:events) if("REVISION_COMPLETED".equals(e.getType()) && Objects.equals(subject,e.getSubject())) return e.getCreatedAt();
        return null;
    }

    private static int reviewInterval(int accuracy,long days){
        if(accuracy<50) return 1;
        if(accuracy<65) return 2;
        if(accuracy<80) return 4;
        if(days>=7) return 14;
        if(days>=4) return 7;
        return 3;
    }
    private static String adaptiveLevel(List<QuizAttempt> qs){
        if(qs.isEmpty()) return "Beginner";
        double avg=qs.stream().mapToInt(QuizAttempt::getScore).average().orElse(0);
        if(avg>=80) return "Advanced";
        if(avg>=60) return "Intermediate";
        return "Beginner";
    }
    private static String adaptiveReason(String level){
        if("Advanced".equals(level)) return "Your recent accuracy is strong, so Nova prioritizes harder challenges.";
        if("Intermediate".equals(level)) return "Your recent accuracy is steady, so Nova mixes medium questions with targeted review.";
        return "Nova is reinforcing fundamentals and prioritizing questions close to your current level.";
    }
    private static int difficultyRank(String value){
        if(value==null) return 1; String v=value.toLowerCase(Locale.ROOT);
        if(v.contains("hard")||v.contains("advanced")) return 2;
        if(v.contains("medium")||v.contains("intermediate")) return 1;
        return 0;
    }
    private static List<String> parseWrong(QuizAttempt q){if(q.getWrongQuestionIds()==null||q.getWrongQuestionIds().isBlank())return List.of();return Arrays.stream(q.getWrongQuestionIds().split(",")).map(String::trim).filter(x->!x.isBlank()).toList();}
}
