package com.project.render.Repository;

import com.project.render.Entity.OwnerApplication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerApplicationRepository extends MongoRepository<OwnerApplication, String> {
    Optional<OwnerApplication> findTopByUserIdOrderByCreatedAtDesc(String userId);
    List<OwnerApplication> findByStatus(String status);
    long deleteByUserId(String userId);
}
