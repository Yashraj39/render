package com.project.render.Repository;

import com.project.render.Entity.Salon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SalonRepository extends MongoRepository<Salon, String> {

    boolean existsByName(String name);

    boolean existsBySalonOwnerId(String salonOwnerId);

    List<Salon> findBySalonOwnerId(String salonOwnerId);

    long deleteBySalonOwnerId(String salonOwnerId);

    List<Salon> findByIsVerifiedTrue();

    List<Salon> findByIsVerifiedFalse();
}