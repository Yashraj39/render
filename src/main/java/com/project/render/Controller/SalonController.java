package com.project.render.Controller;

import com.project.render.DTO.SalonCardResponse;
import com.project.render.Entity.Salon;
import com.project.render.Entity.Service;
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
    public Salon addSalon(@RequestParam String ownerId,
                          @RequestParam String name,
                          @RequestParam String city,
                          @RequestParam String address,
                          @RequestParam String opentime,
                          @RequestParam String closetime,
                          @RequestParam(required = false) MultipartFile image) {
        return salonService.addSalon(ownerId, name, city, address, opentime, closetime, image);
    }

    @GetMapping("/get-salon/{salonId}")
    public Salon getSalon(@PathVariable String salonId){
        return salonService.getSalon(salonId);
    }

    @GetMapping("/get-all-salon")
    public List<SalonCardResponse> getAll(){
        return salonService.getAllSalonWithServices();
    }

}
