package com.aitutor.rag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.aitutor.security.AuthenticatedUser;
import com.aitutor.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController @RequestMapping("/api/knowledge")
public class RagController {
 private final RagService rag; private final AuthenticatedUser authenticatedUser; public RagController(RagService r, AuthenticatedUser au){rag=r;authenticatedUser=au;}
 @GetMapping public List<Map<String,Object>> list(Authentication authentication){ User u=authenticatedUser.require(authentication); return rag.list(u,isAdmin(u)); }
 @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Map<String,Object> upload(@RequestPart("file") MultipartFile file, Authentication authentication) throws IOException { User u=authenticatedUser.require(authentication); var d=rag.upload(file,u); return Map.of("id",d.id,"fileName",d.fileName,"sizeBytes",d.sizeBytes,"message","PDF indexed successfully"); }
 @GetMapping("/{id}/chunks") public List<Map<String,Object>> chunks(@PathVariable Long id, Authentication authentication){ User u=authenticatedUser.require(authentication); return rag.chunks(id,u,isAdmin(u)); }
 @PostMapping("/{id}/pack") public AiRagAnswer pack(@PathVariable Long id,@RequestBody PackRequest r, Authentication authentication){ User u=authenticatedUser.require(authentication); var a=rag.generatePack(id,u,isAdmin(u),r.type(),r.level(),r.language());return new AiRagAnswer(a.answer(),a.provider(),"Generated from indexed PDF context.");}
 @PostMapping("/{id}/ask") public AiRagAnswer ask(@PathVariable Long id,@RequestBody AskRequest r, Authentication authentication){ User u=authenticatedUser.require(authentication); var a=rag.ask(id,u,isAdmin(u),r.question(),r.level(),r.language());return new AiRagAnswer(a.answer(),a.provider(),"Retrieved document context and generated a grounded answer.");}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id, Authentication authentication){ User u=authenticatedUser.require(authentication); rag.delete(id,u,isAdmin(u)); }
 private boolean isAdmin(User u){return "ADMIN".equalsIgnoreCase(u.getRole()) || "OWNER".equalsIgnoreCase(u.getRole());}
 public record AskRequest(String question,String level,String language){} public record PackRequest(String type,String level,String language){} public record AiRagAnswer(String answer,String provider,String retrieval){}
}
