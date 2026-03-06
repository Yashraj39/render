package com.project.render.Controller;

import com.project.render.DTO.ServiceCategoryUpdateRequest;
import com.project.render.Entity.ServiceCategory;
import com.project.render.Repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/service-category")
public class AdminServiceCategoryController {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @PostMapping("/create")
    public ServiceCategory create(@RequestBody ServiceCategory category) {
        category.setId(null);
        return serviceCategoryRepository.save(category);
    }

    @PatchMapping("/update/{categoryId}")
    public ServiceCategory update(
            @PathVariable String categoryId,
            @RequestBody ServiceCategoryUpdateRequest req
    ) {
        ServiceCategory c = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());

        return serviceCategoryRepository.save(c);
    }

    @DeleteMapping("/delete/{categoryId}")
    public String delete(@PathVariable String categoryId) {
        serviceCategoryRepository.deleteById(categoryId);
        return "Master category deleted";
    }

}