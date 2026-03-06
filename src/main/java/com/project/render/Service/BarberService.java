package com.project.render.Service;

import com.project.render.DTO.BarberUpdateRequest;
import com.project.render.Entity.Barber;
import com.project.render.Entity.Salon;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BarberService {

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private SalonRepository salonRepository;

    public Barber addBarber(String salonId, Barber barber){

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

        return savedBarber;
    }

    public List<Barber> getBarbersBySalon(String salonId) {
        return barberRepository.findBySalonId(salonId);
    }

    public Barber updateLeaves(String barberId, List<LocalDate> leaves) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        barber.setLeaves(leaves);
        return barberRepository.save(barber);
    }

    public Barber updateWeeklyOff(String barberId, Set<DayOfWeek> weeklyOffDays) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        barber.setWeeklyOffDays(weeklyOffDays);
        return barberRepository.save(barber);
    }

    public Barber updateBarber(String barberId, BarberUpdateRequest request){

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        if(request.getActive() != null){
            barber.setActive(request.getActive());
        }

        if(request.getWorkingStartTime() != null){
            barber.setWorkingStartTime(request.getWorkingStartTime());
        }

        if(request.getWorkingEndTime() != null){
            barber.setWorkingEndTime(request.getWorkingEndTime());
        }

        if(request.getLunchStart() != null){
            barber.setLunchStart(request.getLunchStart());
        }

        if(request.getLunchEnd() != null){
            barber.setLunchEnd(request.getLunchEnd());
        }

        if(request.getLeaves() != null){
            barber.setLeaves(request.getLeaves());
        }

        if(request.getWeeklyOffDays() != null){
            barber.setWeeklyOffDays(request.getWeeklyOffDays());
        }

        return barberRepository.save(barber);
    }

    public String deleteBarber(String barberId) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Salon salon = salonRepository.findById(barber.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getBarbersIds() != null) {
            salon.getBarbersIds().remove(barberId);
            salonRepository.save(salon);
        }

        barberRepository.delete(barber);

        return "barber deleted successfully";
    }
}
