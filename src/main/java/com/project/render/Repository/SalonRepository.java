package com.project.render.Repository;

import com.project.render.Entity.Salon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SalonRepository extends MongoRepository<Salon,String> {
    boolean existsByName(String name);
}
