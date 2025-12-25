package com.project.render.Controller;

import com.project.render.Entity.ServiceCategory;
import com.project.render.Service.ServiceCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired
    private ServiceCrudService serviceCrudService;

    @PostMapping("/add-service/{salonId}")
    public ServiceCategory addService(@PathVariable String salonId, @RequestBody ServiceCategory serviceCategory) {
        return serviceCrudService.addService(salonId, serviceCategory);
    }

    @GetMapping("/get-service/{serviceId}")
    public Optional<ServiceCategory> getService(@PathVariable String serviceId){
        return serviceCrudService.getService(serviceId);
    }

}
