package com.project.render.Repository;

import com.project.render.Entity.Barber;
import com.project.render.Entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface BarberRepository extends MongoRepository<Barber,String> {

    List<Barber> findBySalonId(String salonId);

    List<Barber> findBySalonIdAndActiveTrue(String salonId);

}

