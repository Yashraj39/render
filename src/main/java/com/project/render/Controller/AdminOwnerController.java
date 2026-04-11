package com.project.render.Controller;

import com.project.render.DTO.AdminDecisionRequest;
import com.project.render.DTO.AdminLoginRequest;
import com.project.render.DTO.AdminLoginResponse;
import com.project.render.DTO.AdminOwnerActionRequest;
import com.project.render.DTO.AdminOwnerMessageRequest;
import com.project.render.DTO.OwnerSummaryResponse;
import com.project.render.Entity.OwnerApplication;
import com.project.render.Entity.Salon;
import com.project.render.Service.AdminAuthService;
import com.project.render.Service.OwnerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/owner")
@CrossOrigin(origins = {"http://localhost:5173", "https://salon-frontend-vercel-project.vercel.app"})
public class AdminOwnerController {

    @Autowired
    private OwnerApplicationService ownerApplicationService;

    @Autowired
    private AdminAuthService adminAuthService;

    @GetMapping("/unverified-salons")
    public List<Salon> getUnverifiedSalons() {
        return ownerApplicationService.getUnverifiedSalons();
    }

    @GetMapping("/applications")
    public List<OwnerApplication> list(@RequestParam String status) {
        return ownerApplicationService.listByStatus(status);
    }

    @PatchMapping("/applications/{id}/approve")
    public OwnerApplication approve(@PathVariable String id, @RequestBody AdminDecisionRequest req) {
        return ownerApplicationService.approve(id, req);
    }

    @PatchMapping("/applications/{id}/reject")
    public String reject(@PathVariable String id, @RequestBody AdminDecisionRequest req) {
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

    @PatchMapping("/reject-salon/{salonId}")
    public String rejectSalon(
            @PathVariable String salonId,
            @RequestParam String adminId,
            @RequestParam String reason
    ) {
        return ownerApplicationService.rejectSalon(salonId, adminId, reason);
    }

    @GetMapping("/manage")
    public List<OwnerSummaryResponse> getAllOwners() {
        return ownerApplicationService.getAllOwners();
    }

    @PatchMapping("/freeze/{ownerUserId}")
    public String freezeOwner(
            @PathVariable String ownerUserId,
            @RequestBody AdminOwnerActionRequest req
    ) {
        return ownerApplicationService.freezeOwner(ownerUserId, req);
    }

    @PatchMapping("/unfreeze/{ownerUserId}")
    public String unfreezeOwner(
            @PathVariable String ownerUserId,
            @RequestBody AdminOwnerActionRequest req
    ) {
        return ownerApplicationService.unfreezeOwner(ownerUserId, req);
    }

    @DeleteMapping("/remove/{ownerUserId}")
    public String removeOwner(
            @PathVariable String ownerUserId,
            @RequestBody AdminOwnerActionRequest req
    ) {
        return ownerApplicationService.removeOwner(ownerUserId, req);
    }

    @PostMapping("/notify/{ownerUserId}")
    public String notifyOwner(
            @PathVariable String ownerUserId,
            @RequestBody AdminOwnerMessageRequest req
    ) {
        return ownerApplicationService.sendAdminMessageToOwner(ownerUserId, req);
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }
}