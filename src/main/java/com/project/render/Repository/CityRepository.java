package com.project.render.Repository;

import com.project.render.Entity.City;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByNameIgnoreCase(String name);
    List<City> findByActiveTrueOrderByNameAsc();
    List<City> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}