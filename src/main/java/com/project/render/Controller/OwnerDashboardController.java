package com.project.render.Controller;

import com.project.render.DTO.OwnerDashboardResponse;
import com.project.render.Service.OwnerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerDashboardController {

    @Autowired
    private OwnerDashboardService ownerDashboardService;

    @GetMapping("/dashboard")
    public OwnerDashboardResponse getDashboard(
            @RequestParam String salonId
    ) {
        return ownerDashboardService.getDashboard(salonId);
    }
}