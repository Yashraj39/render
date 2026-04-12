package com.project.render.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.render.DTO.ServiceUpdateRequest;
import com.project.render.Entity.Salon;
import com.project.render.Entity.Service;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class ServiceCrudService {

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    public Service addService(
            String ownerId,
            String salonId,
            String categoryId,
            Service service,
            MultipartFile image
    ) {
        ownerApplicationService.validateOwnerAccess(ownerId);

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (!ownerId.equals(salon.getSalonOwnerId())) {
            throw new RuntimeException("Unauthorized: you cannot manage this salon");
        }

        if (salon.getServiceIds() == null || !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category not selected for this salon");
        }

        serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

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

        service.setCategoryId(categoryId);
        service.setSalonId(salonId);

        return serviceCrudRepository.save(service);
    }

    public List<Service> getService(String salonId, String categoryId, String genderCategory) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getServiceIds() == null || !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category does not belong to this salon");
        }

        serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Service Not Exists"));

        if (genderCategory.equalsIgnoreCase("all")) {
            return serviceCrudRepository.findBySalonIdAndCategoryId(salonId, categoryId);
        }

        return serviceCrudRepository.findBySalonIdAndCategoryIdAndGenderCategoryIgnoreCase(
                salonId, categoryId, genderCategory
        );
    }

    public List<Service> getAiServices(String salonId, String categoryId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getServiceIds() == null || !salon.getServiceIds().contains(categoryId)) {
            throw new RuntimeException("Category does not belong to this salon");
        }

        return serviceCrudRepository.findByCategoryId(categoryId);
    }

    public Service updateService(
            String ownerId,
            String serviceId,
            ServiceUpdateRequest request,
            MultipartFile image
    ) {
        ownerApplicationService.validateOwnerAccess(ownerId);

        Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Salon salon = salonRepository.findById(service.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (!ownerId.equals(salon.getSalonOwnerId())) {
            throw new RuntimeException("Unauthorized: you cannot manage this service");
        }

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

    public void deleteService(String ownerId, String categoryId, String serviceId) {
        ownerApplicationService.validateOwnerAccess(ownerId);

        serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Salon salon = salonRepository.findById(service.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (!ownerId.equals(salon.getSalonOwnerId())) {
            throw new RuntimeException("Unauthorized: you cannot manage this service");
        }

        if (!categoryId.equals(service.getCategoryId())) {
            throw new RuntimeException("Service does not belong to this category");
        }

        serviceCrudRepository.deleteById(serviceId);
    }
}