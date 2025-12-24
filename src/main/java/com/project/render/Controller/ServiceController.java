package com.project.render.Controller;

import com.project.render.Entity.Salon;
import com.project.render.Entity.Service;
import com.project.render.Service.ServiceCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired
    private ServiceCrudService serviceCrudService;

    @PostMapping("/add-service/{salonId}")
    public Service addService(@PathVariable String salonId, @RequestBody Service service) {
        return serviceCrudService.addService(salonId, service);
    }

    @GetMapping("/get-service/{serviceId}")
    public Optional<Service> getService(@PathVariable String serviceId){
        return serviceCrudService.getService(serviceId);
    }

}
