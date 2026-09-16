package com.aitutor.intelligence;

import com.aitutor.repository.UserRepository;
import com.aitutor.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adaptive")
public class AdaptivePlanController {
    private final LearningEventRepository events; private final UserRepository users; private final AuthenticatedUser auth;
    public AdaptivePlanController(LearningEventRepository e, UserRepository u, AuthenticatedUser a){events=e;users=u;auth=a;}

    @GetMapping("/plan/{userId}")
    public Map<String,Object> plan(@PathVariable Long userId, Authentication authentication){
        auth.requireSame(authentication,userId); users.findById(userId).orElseThrow();
        List<LearningEvent> all=events.findTop200ByUserIdOrderByCreatedAtDesc(userId);
        Map<String,List<LearningEvent>> groups=all.stream().filter(e->e.getTopic()!=null).collect(Collectors.groupingBy(e->Objects.toString(e.getSubject(),"Unknown")+"\u0000"+e.getTopic(),LinkedHashMap::new,Collectors.toList()));
        List<Map<String,Object>> ranked=groups.entrySet().stream().map(e->{double avg=e.getValue().stream().filter(x->x.getScore()!=null).mapToInt(LearningEvent::getScore).average().orElse(50); long misses=e.getValue().stream().filter(x->Objects.equals(x.getScore(),0)).count(); String[] k=e.getKey().split("\u0000",2); int priority=(int)Math.min(100,Math.round((100-avg)*.75+misses*8)); return Map.<String,Object>of("subject",k[0],"topic",k.length>1?k[1]:"Core Concepts","mastery",Math.round(avg),"priority",priority,"misses",misses);}).sorted((a,b)->Integer.compare(((Number)b.get("priority")).intValue(),((Number)a.get("priority")).intValue())).limit(8).toList();
        List<Map<String,Object>> today=new ArrayList<>();
        for(int i=0;i<Math.min(3,ranked.size());i++){var x=ranked.get(i); String mode=i==0?"RECOVER":i==1?"PRACTICE":"RETAIN"; today.add(Map.of("order",i+1,"mode",mode,"topic",x.get("topic"),"subject",x.get("subject"),"minutes",i==0?25:20,"action",mode.equals("RECOVER")?"Explain the concept in simple words":mode.equals("PRACTICE")?"Solve 5 targeted questions":"Recall without notes and review one mistake"));}
        return Map.of("date",LocalDate.now(),"items",today,"generatedFromEvents",all.size(),"adaptive",true,"fallback",ranked.isEmpty());
    }
}
