package com.project.render.Repository;

import com.project.render.Entity.Service;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceCrudRepository extends MongoRepository<Service,String> {
}
