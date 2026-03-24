package com.project.render.Controller;

import com.project.render.DTO.AdminDecisionRequest;
import com.project.render.DTO.AdminLoginRequest;
import com.project.render.DTO.AdminLoginResponse;
import com.project.render.Entity.OwnerApplication;
import com.project.render.Entity.Salon;
import com.project.render.Service.AdminAuthService;
import com.project.render.Service.OwnerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/owner")
public class AdminOwnerController {

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private AdminAuthService adminAuthService;

    @GetMapping("/applications")
    public List<OwnerApplication> list(@RequestParam String status) {
        return ownerApplicationService.listByStatus(status);
    }

    @PatchMapping("/applications/{id}/approve")
    public OwnerApplication approve(@PathVariable String id, @RequestBody AdminDecisionRequest req) {
        return ownerApplicationService.approve(id, req);
    }

    @PatchMapping("/applications/{id}/reject")
    public OwnerApplication reject(@PathVariable String id, @RequestBody AdminDecisionRequest req) {
        return ownerApplicationService.reject(id, req);
    }

    @PatchMapping("/verify-salon/{salonId}")
    public Salon verifySalon(
            @PathVariable String salonId,
            @RequestParam String adminId,
            @RequestParam(required = false) String note
    ) {
        return ownerApplicationService.verifySalon(salonId, adminId, note);
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }

}