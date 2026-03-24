package com.project.render.Controller;

import com.project.render.DTO.CityRequest;
import com.project.render.Entity.City;
import com.project.render.Service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/city")
@CrossOrigin(origins = {"http://localhost:5173", "https://salon-frontend-vercel-project.vercel.app"})
public class CityController {

    @Autowired
    private CityService cityService;

    @PostMapping("/admin/add")
    public City addCity(@RequestBody CityRequest request) {
        return cityService.addCity(request);
    }

    @GetMapping("/admin/all")
    public List<City> getAllCitiesForAdmin() {
        return cityService.getAllCitiesForAdmin();
    }

    @GetMapping("/owner/active")
    public List<City> getActiveCitiesForOwner() {
        return cityService.getActiveCitiesForOwner();
    }

    @PatchMapping("/admin/toggle/{cityId}")
    public City toggleCityStatus(
            @PathVariable String cityId,
            @RequestParam String adminId
    ) {
        return cityService.toggleCityStatus(cityId, adminId);
    }

    @DeleteMapping("/admin/delete/{cityId}")
    public String deleteCity(
            @PathVariable String cityId,
            @RequestParam String adminId
    ) {
        return cityService.deleteCity(cityId, adminId);
    }
}