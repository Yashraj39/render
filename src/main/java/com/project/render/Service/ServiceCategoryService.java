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

    public void addCategoryToSalon(String salonId, String categoryId) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (salon.getServiceIds() == null) salon.setServiceIds(new ArrayList<>());

        if (!salon.getServiceIds().contains(categoryId)) {
            salon.getServiceIds().add(categoryId);
            salonRepository.save(salon);
        }
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

    public List<ServiceCategoryDropdownDTO> getAllMasterCategories() {
        List<ServiceCategory> list = serviceCategoryRepository.findAll();
        List<ServiceCategoryDropdownDTO> res = new ArrayList<>();
        for (ServiceCategory c : list) {
            res.add(new ServiceCategoryDropdownDTO(c.getId(), c.getName()));
        }
        return res;
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

        // ✅ Detach from THIS salon only
        salon.getServiceIds().remove(categoryId);
        salonRepository.save(salon);

        // ✅ Delete only THIS salon's services under that category (optional but recommended)
        serviceCrudRepository.deleteBySalonIdAndCategoryId(salonId, categoryId);

        // ❌ DO NOT delete the category document
    }
}
