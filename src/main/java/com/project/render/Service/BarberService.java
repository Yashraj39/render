package com.project.render.Service;

import com.project.render.Entity.Barber;
import com.project.render.Entity.Salon;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BarberService {

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private SalonRepository salonRepository;

    public String addBarber(String salonId,Barber barber){

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(()->new RuntimeException("Salon not found"));

        barber.setSalonId(salonId);
        barber.setActive(true);
        barberRepository.save(barber);

        if(salon.getBarbersIds() == null){
            salon.setBarbersIds(new ArrayList<>());
        }

        if (!salon.getBarbersIds().contains(barber.getId())) {
            salon.getBarbersIds().add(barber.getId());
        }

        salonRepository.save(salon);

        return "Barber Added";

    }
}
