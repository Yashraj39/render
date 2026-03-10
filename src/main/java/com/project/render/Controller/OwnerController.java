package com.project.render.Controller;

import com.project.render.DTO.OwnerApplyRequest;
import com.project.render.DTO.OwnerBookingsPageResponse;
import com.project.render.Entity.DocumentType;
import com.project.render.Entity.OwnerApplication;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.UserRepository;
import com.project.render.Service.OwnerApplicationService;
import com.project.render.Service.OwnerBookingService;
import com.project.render.Service.SalonService;
import jakarta.mail.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("api/owner")
public class OwnerController {

    @Autowired
    private SalonService salonService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private OwnerBookingService ownerBookingService;

    @PostMapping("/add-salon")
    public Salon addSalon(
            @RequestParam String ownerId,
            @RequestParam String name,
            @RequestParam String city,
            @RequestParam String address,
            @RequestParam String contact,
            @RequestParam String salonEmail,
            @RequestParam String opentime,
            @RequestParam String closetime,
            @RequestParam(required = false) String mapLink,

            @RequestParam(required = false) MultipartFile cover,
            @RequestParam(required = false) MultipartFile interior,
            @RequestParam(required = false) MultipartFile exterior,
            @RequestParam(required = false) MultipartFile ownerPhoto,

            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) MultipartFile document
    ) {
        return salonService.addSalon(
                ownerId, name, city, address, contact, salonEmail,
                opentime, closetime, mapLink,
                cover, interior, exterior, ownerPhoto,
                documentType, document
        );
    }

    @PostMapping("/apply")
    public OwnerApplication apply(@RequestBody OwnerApplyRequest req) {
        return ownerApplicationService.submit(req);
    }

    @GetMapping("/application")
    public OwnerApplication application(@RequestParam String userId) {
        return ownerApplicationService.latest(userId);
    }

    @DeleteMapping("/remove-owner")
    public String removeOwner(@RequestParam String userId) { return ownerApplicationService.removeOwner(userId); }

    @GetMapping("/bookings")
    public ResponseEntity<OwnerBookingsPageResponse> getOwnerBookings(
            @RequestParam String ownerId,
            @RequestParam(required = false) String salonId,
            @RequestParam(required = false) String barberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ownerBookingService.getOwnerBookings(ownerId, salonId, date, barberId, status, page, size)
        );
    }

}
