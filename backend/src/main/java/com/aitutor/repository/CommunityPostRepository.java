package com.aitutor.repository;

import com.aitutor.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findTop100ByOrderByCreatedAtDesc();
}
