package com.project.render.Service;

import com.project.render.Entity.Barber;
import com.project.render.Entity.Salon;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BarberService {

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private SalonRepository salonRepository;

    public String addBarber(String salonId, Barber barber){

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        barber.setSalonId(salonId);
        barber.setActive(true);

        Barber savedBarber = barberRepository.save(barber);

        if(salon.getBarbersIds() == null){
            salon.setBarbersIds(new ArrayList<>());
        }

        if (!salon.getBarbersIds().contains(savedBarber.getId())) {
            salon.getBarbersIds().add(savedBarber.getId());
        }

        salonRepository.save(salon);

        return "Barber Added Successfully";
    }

    public List<Barber> getBarbersBySalon(String salonId) {
        return barberRepository.findBySalonIdAndActiveTrue(salonId);
    }

}
