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

    @PostMapping("/add-service-category/{salonId}/{categoryId}")
    public String attachCategoryToSalon(
            @PathVariable String salonId,
            @PathVariable String categoryId
    ) {
        serviceCategoryService.addCategoryToSalon(salonId, categoryId);
        return "Category attached to salon";
    }

    @GetMapping("/get-all-master-categories")
    public List<ServiceCategoryDropdownDTO> getAllMasterCategories() {
        return serviceCategoryService.getAllMasterCategories();
    }

    @GetMapping("/get-service-categories/{salonId}")
    public List<ServiceCategoryDropdownDTO> getServices(@PathVariable String salonId){
        return serviceCategoryService.getCategoryBySalonId(salonId);
    }

    @GetMapping("/get-service-category/{serviceId}")
    public Optional<ServiceCategory> getService(@PathVariable String serviceId){
        return serviceCategoryService.getService(serviceId);
    }

    @PatchMapping("/update-service-category/{categoryId}")
    public ServiceCategory updateCategory(
            @PathVariable String categoryId,
            @RequestBody com.project.render.DTO.ServiceCategoryUpdateRequest request
    ) {
        return serviceCategoryService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/delete-service-category/{salonId}/{categoryId}")
    public String deleteCategory(
            @PathVariable String salonId,
            @PathVariable String categoryId
    ) {
        serviceCategoryService.deleteCategory(salonId, categoryId);
        return "Category Deleted";
    }


}
