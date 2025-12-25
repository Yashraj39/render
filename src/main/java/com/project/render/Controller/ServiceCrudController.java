package com.project.render.Controller;

import com.project.render.Entity.Service;
import com.project.render.Service.ServiceCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add-service/{serviceCategoryId}")
    public Service addService(@PathVariable String serviceCategoryId, @RequestBody Service service){
        return serviceCrudService.addService(serviceCategoryId,service);
    }
}
