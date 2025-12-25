package com.project.render.Repository;

import com.project.render.Entity.Service;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceCrudRepository extends MongoRepository<Service,String> {

    List<Service> findByCategoryId(String categoryId);

    List<Service> findByCategoryIdAndGenderCategoryIgnoreCase(String categoryId,String genderCategory);

}
