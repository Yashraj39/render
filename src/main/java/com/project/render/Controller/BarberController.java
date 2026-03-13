package com.project.render.Controller;

import com.project.render.DTO.BarberCreateRequest;
import com.project.render.DTO.BarberLeaveRequest;
import com.project.render.DTO.BarberTemporaryInactiveRequest;
import com.project.render.DTO.BarberUpdateRequest;
import com.project.render.DTO.BarberWeeklyOffRequest;
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
    public Barber addBarber(
            @PathVariable String salonId,
            @RequestBody BarberCreateRequest request
    ) {
        return barberService.addBarber(salonId, request);
    }

    @GetMapping("/salon/{salonId}")
    public List<Barber> getBarbersBySalon(@PathVariable String salonId) {
        return barberService.getBarbersBySalon(salonId);
    }

    @PatchMapping("/{barberId}/leaves")
    public Barber updateLeaves(@PathVariable String barberId, @RequestBody BarberLeaveRequest request) {
        return barberService.updateLeaves(barberId, request.getLeaves(), request.getAutoCancelConflictingBookings(), request.getCancellationReason());
    }

    @PatchMapping("/{barberId}/weekly-off")
    public Barber updateWeeklyOff(@PathVariable String barberId, @RequestBody BarberWeeklyOffRequest request) {
        return barberService.updateWeeklyOff(barberId, request.getWeeklyOffDays(), request.getAutoCancelConflictingBookings(), request.getCancellationReason());
    }

    @PatchMapping("/{barberId}")
    public Barber updateBarber(@PathVariable String barberId, @RequestBody BarberUpdateRequest request) {
        return barberService.updateBarber(barberId, request);
    }

    @PostMapping("/{barberId}/temporary-inactive")
    public Barber markTemporaryInactive(@PathVariable String barberId, @RequestBody BarberTemporaryInactiveRequest request) {
        return barberService.markTemporaryInactive(barberId, request);
    }

    @DeleteMapping("/{barberId}")
    public String deleteBarber(@PathVariable String barberId) {
        return barberService.deleteBarber(barberId);
    }
}