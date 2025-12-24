package com.project.render.Controller;

import com.project.render.Entity.Salon;
import com.project.render.Service.SalonService;
import jakarta.mail.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/owner")
public class OwnerController {

    @Autowired
    private SalonService salonService;

    @PostMapping("/add-salon")
    public Salon addSalon(@RequestParam String ownerId,
                          @RequestParam String name,
                          @RequestParam String city,
                          @RequestParam String address,
                          @RequestParam String contact,
                          @RequestParam String salonEmail,
                          @RequestParam String opentime,
                          @RequestParam String closetime,
                          @RequestParam(required = false) MultipartFile image) {
        return salonService.addSalon(ownerId, name, city, address, contact, salonEmail, opentime, closetime, image);
    }

}
