package com.project.render.Repository;

import com.project.render.Entity.BookingCart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookingCartRepository extends MongoRepository<BookingCart,String> {

    Optional<BookingCart> findByUserIdAndSalonId(String userId, String salonId);

    List<BookingCart> findByUserId(String userId);

}
