package com.project.render.Repository;

import com.project.render.Entity.ServiceCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRepository extends MongoRepository<ServiceCategory,String> {

}
