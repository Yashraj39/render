package com.project.render.Service;

import com.project.render.Entity.Salon;
import com.project.render.Entity.Service;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceCrudService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SalonRepository salonRepository;

    public Service addService(String salonId,com.project.render.Entity.Service service) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(()->new RuntimeException("Salon not found"));

        Service savedService = serviceRepository.save(service);

        if(salon.getServiceIds() == null) {
            salon.setServiceIds(new ArrayList<>());
        }

        salon.getServiceIds().add(savedService.getId());

        salonRepository.save(salon);

        return service;
    }

    public Optional<Service> getService(String serviceId) {
        return serviceRepository.findById(serviceId);
    }
}
