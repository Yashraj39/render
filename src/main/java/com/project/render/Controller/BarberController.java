package com.project.render.Controller;

import com.project.render.Entity.Barber;
import com.project.render.Service.BarberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barber")
public class BarberController {

    @Autowired
    private BarberService barberService;

    @PostMapping("/add/{salonId}")
    public String addBarber(
            @PathVariable String salonId,
            @RequestBody Barber barber) {

        return barberService.addBarber(salonId, barber);
    }

    @GetMapping("/salon/{salonId}")
    public List<Barber> getBarbersBySalon(@PathVariable String salonId) {
        return barberService.getBarbersBySalon(salonId);
    }

}