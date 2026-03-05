package com.project.render.Repository;

import com.project.render.Entity.Service;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceCrudRepository extends MongoRepository<Service,String> {

    List<Service> findByCategoryId(String categoryId);

    List<Service> findByCategoryIdAndGenderCategoryIgnoreCase(String categoryId,String genderCategory);

    List<Service> findBySalonIdAndCategoryId(String salonId, String categoryId);

    List<Service> findBySalonIdAndCategoryIdAndGenderCategoryIgnoreCase(
            String salonId, String categoryId, String genderCategory
    );

    void deleteBySalonIdAndCategoryId(String salonId, String categoryId);



}
