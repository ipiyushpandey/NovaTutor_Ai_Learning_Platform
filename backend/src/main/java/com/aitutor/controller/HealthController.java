package com.aitutor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    Map<String,String> h(){ return Map.of("status","UP","app","NovaTutor","note","Use /actuator/health for deployment readiness checks"); }
}
