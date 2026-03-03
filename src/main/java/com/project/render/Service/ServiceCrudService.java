package com.project.render.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.render.DTO.ServiceUpdateRequest;
import com.project.render.Entity.Salon;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServiceCrudService {

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private Cloudinary cloudinary;

    public com.project.render.Entity.Service addService(String serviceCategoryId, com.project.render.Entity.Service service, MultipartFile image){

        ServiceCategory serviceCategory = serviceCategoryRepository
                .findById(serviceCategoryId).orElseThrow(()-> new RuntimeException());

        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap("folder", "services")
                );
                service.setImageUrl(uploadResult.get("secure_url").toString());
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

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

    public List<com.project.render.Entity.Service> getAiServices(String salonId, String categoryId) {

        Salon salon = salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found"));

        if (salon.getServiceIds() == null ||
                !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category does not belong to this salon");
        }

         return serviceCrudRepository.findByCategoryId(categoryId);
    }

    public com.project.render.Entity.Service updateService(
            String serviceId,
            ServiceUpdateRequest request,
            MultipartFile image
    ) {
        com.project.render.Entity.Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (request.getName() != null) service.setName(request.getName());
        if (request.getGenderCategory() != null) service.setGenderCategory(request.getGenderCategory());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getPrice() != null) service.setPrice(request.getPrice());
        if (request.getTime() != null) service.setTime(request.getTime());

        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap("folder", "services")
                );
                service.setImageUrl(uploadResult.get("secure_url").toString());
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        return serviceCrudRepository.save(service);
    }

    public void deleteService(String categoryId, String serviceId) {

        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        com.project.render.Entity.Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!categoryId.equals(service.getCategoryId())) {
            throw new RuntimeException("Service does not belong to this category");
        }

        if (category.getServiceIds() != null) {
            category.getServiceIds().remove(serviceId);
            serviceCategoryRepository.save(category);
        }

        serviceCrudRepository.deleteById(serviceId);
    }
}
