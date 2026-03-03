package com.project.render.Controller;

import com.project.render.DTO.SalonCardResponse;
import com.project.render.DTO.SalonDetails;
import com.project.render.Entity.DocumentType;
import com.project.render.Entity.Salon;
import com.project.render.Service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/salon")
public class SalonController {

    @Autowired
    private SalonService salonService;

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

    @GetMapping("/get-salon/{salonId}")
    public SalonDetails getSalon(@PathVariable String salonId){
        return salonService.getSalonDetails(salonId);
    }

    @GetMapping("/get-all-salon")
    public List<SalonCardResponse> getAll(){
        return salonService.getAllSalonWithServices();
    }

    @GetMapping("get-salon-by-owner/{ownerId}")
    public List<Salon> getSalonByOwnerId(@PathVariable String ownerId) {
        return salonService.getSalonByOwnrId(ownerId);
    }

}
