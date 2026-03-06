package com.project.render.Controller;

import com.project.render.DTO.BarberLeaveRequest;
import com.project.render.DTO.BarberUpdateRequest;
import com.project.render.DTO.BarberWeeklyOffRequest;
import com.project.render.Entity.Barber;
import com.project.render.Service.BarberService;
import com.project.render.Service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barber")
public class BarberController {

    @Autowired
    private BarberService barberService;

    @Autowired
    private SalonService salonService;

    @PostMapping("/add/{salonId}")
    public Barber addBarber(
            @PathVariable String salonId,
            @RequestBody Barber barber) {

        return barberService.addBarber(salonId, barber);
    }

    @GetMapping("/salon/{salonId}")
    public List<Barber> getBarbersBySalon(@PathVariable String salonId) {
        return barberService.getBarbersBySalon(salonId);
    }

    @PatchMapping("/{barberId}/leaves")
    public Barber updateLeaves(@PathVariable String barberId, @RequestBody BarberLeaveRequest request) {
        return barberService.updateLeaves(barberId, request.getLeaves());
    }

    @PatchMapping("/{barberId}/weekly-off")
    public Barber updateWeeklyOff(@PathVariable String barberId, @RequestBody BarberWeeklyOffRequest request) {
        return barberService.updateWeeklyOff(barberId, request.getWeeklyOffDays());
    }

    @PatchMapping("/{barberId}")
    public Barber updateBarber(
            @PathVariable String barberId,
            @RequestBody BarberUpdateRequest request
    ) {
        return barberService.updateBarber(barberId, request);
    }

    @DeleteMapping("/{barberId}")
    public String deleteBarber(
            @PathVariable String barberId
    ){
        return barberService.deleteBarber(barberId);
    }

}