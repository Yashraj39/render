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
import java.util.Optional;

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

    public List<com.project.render.Entity.Service> getService(String salonId, String categoryId, String genderCategory){

        Salon salon = salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found"));

        if (salon.getServiceIds() == null ||
                !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category does not belong to this salon");
        }

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(categoryId).orElseThrow(()-> new RuntimeException("Service Not Exists"));

        if(genderCategory.equalsIgnoreCase("all")){
            return serviceCrudRepository.findByCategoryId(categoryId);
        }

        return serviceCrudRepository.findByCategoryIdAndGenderCategoryIgnoreCase(categoryId,genderCategory);

    }

}
