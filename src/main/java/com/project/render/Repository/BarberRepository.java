package com.project.render.Repository;

import com.project.render.Entity.Barber;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BarberRepository extends MongoRepository<Barber,String> {



}
