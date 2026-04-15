package com.project.render.Service;

import com.project.render.DTO.CityRequest;
import com.project.render.Entity.City;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.CityRepository;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalonRepository salonRepository;

    public City addCity(CityRequest request) {
        if (request.getAdminId() == null || request.getAdminId().trim().isEmpty()) {
            throw new RuntimeException("Admin id is required");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("City name is required");
        }

        User admin = userRepository.findByUserId(request.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Unauthorized");
        }

        String cityName = request.getName().trim().toLowerCase();
        cityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);

        if (cityRepository.existsByNameIgnoreCase(cityName)) {
            throw new RuntimeException("City already exists");
        }

        City city = City.builder()
                .name(cityName)
                .active(true)
                .createdBy(request.getAdminId())
                .createdAt(new Date())
                .build();

        return cityRepository.save(city);
    }

    public List<City> getAllCitiesForAdmin() {
        return cityRepository.findAllByOrderByNameAsc();
    }

    public List<City> getActiveCitiesForOwner() {
        return cityRepository.findByActiveTrueOrderByNameAsc();
    }

    public City toggleCityStatus(String cityId, String adminId) {
        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Unauthorized");
        }

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));

        city.setActive(!city.isActive());
        return cityRepository.save(city);
    }

    public String deleteCity(String cityId, String adminId) {
        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Unauthorized");
        }

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));

        cityRepository.delete(city);
        return "City deleted successfully";
    }

    public City updateCity(String cityId, String adminId, CityRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("City name is required");
        }

        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Unauthorized");
        }

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));

        String oldName = city.getName();   // ✅ STORE OLD NAME

        String newName = request.getName().trim().toLowerCase();
        newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);

        boolean exists = cityRepository.existsByNameIgnoreCase(newName)
                && !city.getName().equalsIgnoreCase(newName);

        if (exists) {
            throw new RuntimeException("City already exists");
        }

        // ✅ UPDATE CITY NAME
        city.setName(newName);
        cityRepository.save(city);

        // 🔥 IMPORTANT: UPDATE ALL SALONS
        List<Salon> salons = salonRepository.findAll();

        for (Salon salon : salons) {
            if (salon.getCity() != null && salon.getCity().equalsIgnoreCase(oldName)) {
                salon.setCity(newName);
                salonRepository.save(salon);
            }
        }

        return city;
    }

    public void validateCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new RuntimeException("City is required");
        }

        City city = cityRepository.findByNameIgnoreCase(cityName.trim())
                .orElseThrow(() -> new RuntimeException("Selected city is not allowed"));

        if (!city.isActive()) {
            throw new RuntimeException("Selected city is inactive");
        }
    }
}