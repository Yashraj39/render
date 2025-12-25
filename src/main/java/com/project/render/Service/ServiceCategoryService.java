package com.project.render.Service;

import com.project.render.Entity.Salon;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    public ServiceCategory addService(String salonId, ServiceCategory serviceCategory) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(()->new RuntimeException("Salon not found"));

        ServiceCategory savedServiceCategory = serviceCategoryRepository.save(serviceCategory);

        if(salon.getServiceIds() == null) {
            salon.setServiceIds(new ArrayList<>());
        }

        salon.getServiceIds().add(savedServiceCategory.getId());

        salonRepository.save(salon);

        return serviceCategory;
    }

    public Optional<ServiceCategory> getService(String serviceId) {
        return serviceCategoryRepository.findById(serviceId);
    }
}
