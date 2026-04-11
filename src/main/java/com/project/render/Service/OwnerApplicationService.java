package com.project.render.Service;

import com.project.render.DTO.AdminDecisionRequest;
import com.project.render.DTO.AdminOwnerActionRequest;
import com.project.render.DTO.AdminOwnerMessageRequest;
import com.project.render.DTO.OwnerApplyRequest;
import com.project.render.DTO.OwnerSummaryResponse;
import com.project.render.Entity.OwnerApplication;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.OwnerApplicationRepository;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OwnerApplicationService {

    @Autowired
    private OwnerApplicationRepository ownerApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private NotificationService notificationService;

    public OwnerApplication submit(OwnerApplyRequest req) {

        if (req.getTermsAccepted() == null || !req.getTermsAccepted())
            throw new RuntimeException("Terms not accepted");

        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("OWNER".equalsIgnoreCase(user.getRole()))
            throw new RuntimeException("Already Owner");

        OwnerApplication last = ownerApplicationRepository
                .findTopByUserIdOrderByCreatedAtDesc(req.getUserId())
                .orElse(null);

        if (last != null && "PENDING".equalsIgnoreCase(last.getStatus()))
            throw new RuntimeException("Application already pending");

        OwnerApplication app = OwnerApplication.builder()
                .userId(req.getUserId())
                .phone(req.getPhone())
                .email(req.getEmail())
                .aadhaarUrl(req.getAadhaarUrl())
                .termsAccepted(true)
                .status("PENDING")
                .build();

        OwnerApplication saved = ownerApplicationRepository.save(app);
        userRepository.save(user);

        return saved;
    }

    public OwnerApplication latest(String userId) {
        return ownerApplicationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public List<OwnerApplication> listByStatus(String status) {
        return ownerApplicationRepository.findByStatus(status);
    }

    public OwnerApplication approve(String applicationId, AdminDecisionRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        OwnerApplication app = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!"PENDING".equalsIgnoreCase(app.getStatus()))
            throw new RuntimeException("This application has already been processed");

        User user = userRepository.findByUserId(app.getUserId())
                .orElseThrow(() -> new RuntimeException("Linked user not found for this application"));

        app.setStatus("APPROVED");
        app.setReviewedBy(req.getAdminId());
        app.setReviewedAt(new Date());
        app.setAdminNote(req.getNote());

        OwnerApplication saved = ownerApplicationRepository.save(app);

        user.setRole("OWNER");
        user.setOwnerFrozen(false);
        user.setFrozenByAdminId(null);
        user.setFreezeReason(null);
        user.setOwnerFrozenAt(null);
        userRepository.save(user);

        notificationService.createNotification(
                user.getUserId(),
                "Owner Application Approved",
                "Congratulations! Your owner application has been approved.",
                "OWNER_APPLICATION_APPROVED",
                "USER"
        );

        return saved;
    }

    public String reject(String applicationId, AdminDecisionRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        OwnerApplication app = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!"PENDING".equalsIgnoreCase(app.getStatus()))
            throw new RuntimeException("This application has already been processed");

        User user = userRepository.findByUserId(app.getUserId())
                .orElseThrow(() -> new RuntimeException("Linked user not found"));

        String reason = (req.getNote() != null && !req.getNote().trim().isEmpty())
                ? req.getNote().trim()
                : "No reason provided";

        notificationService.createNotification(
                app.getUserId(),
                "Owner Application Rejected",
                "Your owner application was rejected. Reason: " + reason,
                "OWNER_APPLICATION_REJECTED",
                "USER"
        );

        user.setRole("USER");
        user.setOwnerFrozen(false);
        user.setFrozenByAdminId(null);
        user.setFreezeReason(null);
        user.setOwnerFrozenAt(null);
        userRepository.save(user);

        ownerApplicationRepository.deleteById(applicationId);

        return "Owner application rejected and deleted successfully";
    }

    public Salon verifySalon(String salonId, String adminId, String note) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("User is not admin");

        salon.setVerified(true);
        salon.setVerificationStatus("APPROVED");
        salon.setRejectionReason(null);
        salon.setVerifiedByAdminId(adminId);
        salon.setAdminNote(note);

        notificationService.createNotification(
                salon.getSalonOwnerId(),
                "Salon Approved",
                "Your salon \"" + salon.getName() + "\" has been approved by admin.",
                "SALON_APPROVED",
                "OWNER"
        );

        return salonRepository.save(salon);
    }

    public String rejectSalon(String salonId, String adminId, String reason) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("User is not admin");

        if (reason == null || reason.trim().isEmpty())
            throw new RuntimeException("Disapprove reason is required");

        notificationService.createNotification(
                salon.getSalonOwnerId(),
                "Salon Rejected",
                "Your salon \"" + salon.getName() + "\" was disapproved by admin. Reason: " + reason.trim(),
                "SALON_REJECTED",
                "OWNER"
        );

        salonRepository.deleteById(salonId);

        return "Salon disapproved and deleted successfully";
    }

    public List<Salon> getUnverifiedSalons() {
        return salonRepository.findByVerificationStatus("PENDING");
    }

    public List<OwnerSummaryResponse> getAllOwners() {
        return userRepository.findByRoleIgnoreCase("OWNER")
                .stream()
                .map(user -> OwnerSummaryResponse.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .ownerFrozen(Boolean.TRUE.equals(user.getOwnerFrozen()))
                        .freezeReason(user.getFreezeReason())
                        .ownerFrozenAt(user.getOwnerFrozenAt())
                        .build())
                .toList();
    }

    public String freezeOwner(String ownerUserId, AdminOwnerActionRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new RuntimeException("Selected user is not an owner");

        if (Boolean.TRUE.equals(owner.getOwnerFrozen()))
            throw new RuntimeException("Owner already frozen");

        String reason = (req.getNote() != null && !req.getNote().trim().isEmpty())
                ? req.getNote().trim()
                : "No reason provided";

        owner.setOwnerFrozen(true);
        owner.setFrozenByAdminId(req.getAdminId());
        owner.setFreezeReason(reason);
        owner.setOwnerFrozenAt(new Date());
        userRepository.save(owner);

        notificationService.createNotification(
                owner.getUserId(),
                "Owner Account Frozen",
                "Your owner access has been frozen by admin. Reason: " + reason,
                "OWNER_FROZEN",
                "OWNER"
        );

        return "Owner frozen successfully";
    }

    public String unfreezeOwner(String ownerUserId, AdminOwnerActionRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new RuntimeException("Selected user is not an owner");

        owner.setOwnerFrozen(false);
        owner.setFrozenByAdminId(null);
        owner.setFreezeReason(null);
        owner.setOwnerFrozenAt(null);
        userRepository.save(owner);

        String note = (req.getNote() != null && !req.getNote().trim().isEmpty())
                ? req.getNote().trim()
                : "Your owner access has been restored.";

        notificationService.createNotification(
                owner.getUserId(),
                "Owner Account Unfrozen",
                note,
                "OWNER_UNFROZEN",
                "OWNER"
        );

        return "Owner unfrozen successfully";
    }

    @Transactional
    public String removeOwner(String ownerUserId, AdminOwnerActionRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new RuntimeException("Selected user is not an owner");

        String note = (req.getNote() != null && !req.getNote().trim().isEmpty())
                ? req.getNote().trim()
                : "Removed by admin";

        notificationService.createNotification(
                owner.getUserId(),
                "Owner Access Removed",
                "Your owner access has been removed by admin. Reason: " + note,
                "OWNER_REMOVED",
                "USER"
        );

        // remove application history
        ownerApplicationRepository.deleteByUserId(ownerUserId);

        // remove salons
        salonRepository.deleteBySalonOwnerId(ownerUserId);

        // IMPORTANT:
        // Add your other owner related cleanup here.
        // Example:
        // barberRepository.deleteBySalonOwnerId(ownerUserId);
        // serviceCategoryRepository.deleteBySalonOwnerId(ownerUserId);
        // bookingRepository.deleteByOwnerId(ownerUserId);
        // notificationRepository.deleteByUserIdAndAudience(ownerUserId, "OWNER");

        owner.setRole("USER");
        owner.setOwnerFrozen(false);
        owner.setFrozenByAdminId(null);
        owner.setFreezeReason(null);
        owner.setOwnerFrozenAt(null);
        userRepository.save(owner);

        return "Owner removed successfully and related data deleted";
    }

    public String sendAdminMessageToOwner(String ownerUserId, AdminOwnerMessageRequest req) {
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new RuntimeException("Selected user is not an owner");

        String title = (req.getTitle() != null && !req.getTitle().trim().isEmpty())
                ? req.getTitle().trim()
                : "Message from Admin";

        String message = (req.getMessage() != null && !req.getMessage().trim().isEmpty())
                ? req.getMessage().trim()
                : "No message provided";

        notificationService.createNotification(
                owner.getUserId(),
                title,
                message,
                "ADMIN_MESSAGE_TO_OWNER",
                "OWNER"
        );

        return "Notification sent to owner successfully";
    }

    public void validateOwnerAccess(String ownerUserId) {
        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"OWNER".equalsIgnoreCase(owner.getRole())) {
            throw new RuntimeException("User is not owner");
        }

        if (Boolean.TRUE.equals(owner.getOwnerFrozen())) {
            throw new RuntimeException("Owner account is frozen by admin");
        }
    }
}