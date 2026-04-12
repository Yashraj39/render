package com.project.render.Controller;

import com.project.render.DTO.BarberCreateRequest;
import com.project.render.DTO.BarberLeaveRequest;
import com.project.render.DTO.BarberTemporaryInactiveRequest;
import com.project.render.DTO.BarberUpdateRequest;
import com.project.render.DTO.BarberVacationRequest;
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
            @RequestParam String ownerId,
            @PathVariable String salonId,
            @RequestBody BarberCreateRequest request
    ) {
        return barberService.addBarber(ownerId, salonId, request);
    }

    @GetMapping("/salon/{salonId}")
    public List<Barber> getBarbersBySalon(@PathVariable String salonId) {
        return barberService.getBarbersBySalon(salonId);
    }

    @PatchMapping("/{barberId}/leaves")
    public Barber updateLeaves(
            @RequestParam String ownerId,
            @PathVariable String barberId,
            @RequestBody BarberLeaveRequest request
    ) {
        return barberService.updateLeaves(
                ownerId,
                barberId,
                request.getLeaves(),
                request.getAutoCancelConflictingBookings(),
                request.getCancellationReason()
        );
    }

    @PatchMapping("/{barberId}/weekly-off")
    public Barber updateWeeklyOff(
            @RequestParam String ownerId,
            @PathVariable String barberId,
            @RequestBody BarberWeeklyOffRequest request
    ) {
        return barberService.updateWeeklyOff(
                ownerId,
                barberId,
                request.getWeeklyOffDays(),
                request.getAutoCancelConflictingBookings(),
                request.getCancellationReason()
        );
    }

    @PatchMapping("/{barberId}")
    public Barber updateBarber(
            @RequestParam String ownerId,
            @PathVariable String barberId,
            @RequestBody BarberUpdateRequest request
    ) {
        return barberService.updateBarber(ownerId, barberId, request);
    }

    @PostMapping("/{barberId}/temporary-inactive")
    public Barber markTemporaryInactive(
            @RequestParam String ownerId,
            @PathVariable String barberId,
            @RequestBody BarberTemporaryInactiveRequest request
    ) {
        return barberService.markTemporaryInactive(ownerId, barberId, request);
    }

    @PostMapping("/{barberId}/cancel-temporary-inactive")
    public Barber cancelTemporaryInactive(
            @RequestParam String ownerId,
            @PathVariable String barberId
    ) {
        return barberService.cancelTemporaryInactive(ownerId, barberId);
    }

    @PatchMapping("/{barberId}/vacation")
    public Barber addVacation(
            @RequestParam String ownerId,
            @PathVariable String barberId,
            @RequestBody BarberVacationRequest request
    ) {
        return barberService.addVacation(
                ownerId,
                barberId,
                request.getStartDate(),
                request.getEndDate(),
                request.getAutoCancelConflictingBookings(),
                request.getCancellationReason()
        );
    }

    @DeleteMapping("/{barberId}")
    public String deleteBarber(
            @RequestParam String ownerId,
            @PathVariable String barberId
    ) {
        return barberService.deleteBarber(ownerId, barberId);
    }
}