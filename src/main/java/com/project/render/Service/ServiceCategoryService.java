package com.project.render.Service;

import com.project.render.DTO.ServiceCategoryDropdownDTO;
import com.project.render.DTO.ServiceCategoryUpdateRequest;
import com.project.render.Entity.Salon;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    public ServiceCategory addService(String salonId, ServiceCategory serviceCategory) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(()->new RuntimeException("Salon not found"));

        ServiceCategory savedServiceCategory = serviceCategoryRepository.save(serviceCategory);

        if(salon.getServiceIds() == null) {
            salon.setServiceIds(new ArrayList<>());
        }

        salon.getServiceIds().add(savedServiceCategory.getId());

        salonRepository.save(salon);

        return savedServiceCategory;
    }

    public Optional<ServiceCategory> getService(String serviceId) {
        return serviceCategoryRepository.findById(serviceId);
    }

    public List<ServiceCategoryDropdownDTO> getCategoryBySalonId(String salonId){
        Salon salon = salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found!"));
        List<ServiceCategoryDropdownDTO> response = new ArrayList<>();

        if(salon.getServiceIds()==null) return response;

        for(String categoryId:salon.getServiceIds()){

            serviceCategoryRepository.findById(categoryId).ifPresent(category ->
                response.add(new ServiceCategoryDropdownDTO(
                        category.getId(),
                        category.getName()
                    )
                )
            );

        }

        return response;
    }

    public ServiceCategory updateCategory(String categoryId, ServiceCategoryUpdateRequest request) {

        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());

        return serviceCategoryRepository.save(category);
    }

    public void deleteCategory(String salonId, String categoryId) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getServiceIds() == null || !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category does not belong to this salon");
        }

        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getServiceIds() != null && !category.getServiceIds().isEmpty()) {
            serviceCrudRepository.deleteAllById(category.getServiceIds());
        }

        salon.getServiceIds().remove(categoryId);
        salonRepository.save(salon);

        serviceCategoryRepository.deleteById(categoryId);
    }
}
