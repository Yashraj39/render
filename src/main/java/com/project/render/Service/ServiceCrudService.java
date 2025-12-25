package com.project.render.Service;

import com.project.render.Entity.Salon;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceCrudService {

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    public com.project.render.Entity.Service addService(String serviceCategoryId,com.project.render.Entity.Service service){

        ServiceCategory serviceCategory = serviceCategoryRepository
                .findById(serviceCategoryId).orElseThrow(()-> new RuntimeException());

        service.setCategoryId(serviceCategoryId);

        com.project.render.Entity.Service savedService = serviceCrudRepository.save(service);

        if (serviceCategory.getServiceIds() == null) {
            serviceCategory.setServiceIds(new ArrayList<>());
        }

        serviceCategory.getServiceIds().add(savedService.getId());

        serviceCategoryRepository.save(serviceCategory);

        return savedService;
    }

    public void getService(String salonId,String categoryId,String genderCategory){

        Salon salon = salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found"));

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(salonId).orElseThrow(()-> new RuntimeException());



    }


}
