package com.project.render.Repository;

import com.project.render.Entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByBarberIdAndBookingDate(String barberId, LocalDate bookingDate);

}


