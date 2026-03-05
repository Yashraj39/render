package com.project.render.Repository;

import com.project.render.Entity.ServiceCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceCategoryRepository extends MongoRepository<ServiceCategory,String> {

}
