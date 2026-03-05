package com.project.render.Controller;

import com.project.render.Entity.Service;
import com.project.render.Service.ServiceCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceCrudController {

    @Autowired
    private ServiceCrudService serviceCrudService;

    @GetMapping("/get-services")
    public List<Service> getService(@RequestParam String salonId,
                                    @RequestParam String categoryId,
                                    @RequestParam(required = false,defaultValue = "all") String gender
    ){
        return serviceCrudService.getService(salonId, categoryId, gender);
    }

    @PostMapping(value = "/add-service/{serviceCategoryId}",consumes = "multipart/form-data")
    public Service addService(
                              @PathVariable String serviceCategoryId,
                              @RequestParam String salonId,
                              @ModelAttribute Service service,
                              @RequestPart(value = "image",required = false)MultipartFile image){
        return serviceCrudService.addService(salonId,serviceCategoryId,service,image);
    }

    @GetMapping("/get-services-for-ai")
    public List<Service> getServicesForAI(
            @RequestParam String salonId,
            @RequestParam String categoryId
    ) {
        return serviceCrudService.getAiServices(salonId,categoryId);
    }

    @PatchMapping(value = "/update-service/{serviceId}", consumes = "multipart/form-data")
    public Service updateService(
            @PathVariable String serviceId,
            @ModelAttribute com.project.render.DTO.ServiceUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return serviceCrudService.updateService(serviceId, request, image);
    }

    @DeleteMapping("/delete-service/{categoryId}/{serviceId}")
    public String deleteService(
            @PathVariable String categoryId,
            @PathVariable String serviceId
    ) {
        serviceCrudService.deleteService(categoryId, serviceId);
        return "Service Deleted";
    }


}
