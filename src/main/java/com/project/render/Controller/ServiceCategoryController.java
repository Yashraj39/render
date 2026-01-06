package com.project.render.Controller;

import com.project.render.DTO.ServiceCategoryDropdownDTO;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/service-category")
public class ServiceCategoryController {

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @PostMapping("/add-service-category/{salonId}")
    public ServiceCategory addService(@PathVariable String salonId, @RequestBody ServiceCategory serviceCategory) {
        return serviceCategoryService.addService(salonId, serviceCategory);
    }

    @GetMapping("/get-service-categories/{salonId}")
    public List<ServiceCategoryDropdownDTO> getServices(@PathVariable String salonId){
        return serviceCategoryService.getCategoryBySalonId(salonId);
    }

    @GetMapping("/get-service-category/{serviceId}")
    public Optional<ServiceCategory> getService(@PathVariable String serviceId){
        return serviceCategoryService.getService(serviceId);
    }

}
