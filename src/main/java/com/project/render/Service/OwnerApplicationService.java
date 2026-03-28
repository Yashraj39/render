package com.project.render.Service;

import com.project.render.DTO.AdminDecisionRequest;
import com.project.render.DTO.OwnerApplyRequest;
import com.project.render.Entity.OwnerApplication;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.OwnerApplicationRepository;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Timestamp;
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

    public OwnerApplication submit(OwnerApplyRequest req){

        if(req.getTermsAccepted() == null || !req.getTermsAccepted())
            throw new RuntimeException("Terms not accepted");

        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(()-> new RuntimeException("User not found"));

        if("OWNER".equalsIgnoreCase(user.getRole()))
            throw new RuntimeException("Already Owner");

        OwnerApplication last = ownerApplicationRepository
                .findTopByUserIdOrderByCreatedAtDesc(req.getUserId())
                .orElse(null);

        if(last != null && "PENDING".equalsIgnoreCase(last.getStatus()))
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

    public OwnerApplication latest(String userId){
        return ownerApplicationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(()->new RuntimeException("Application not found"));
    }

    public List<OwnerApplication> listByStatus(String status){
        return ownerApplicationRepository.findByStatus(status);
    }

    public OwnerApplication approve(String applicationId, AdminDecisionRequest req){
        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if(!"ADMIN".equalsIgnoreCase(admin.getRole()))
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
        userRepository.save(user);

        return saved;
    }

    public OwnerApplication reject(String applicationId, AdminDecisionRequest req) {

        User admin = userRepository.findByUserId(req.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equals(admin.getRole()))
            throw new RuntimeException("Unauthorized");

        OwnerApplication app = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!"PENDING".equalsIgnoreCase(app.getStatus()))
            throw new RuntimeException("This application has already been processed");

        app.setStatus("REJECTED");
        app.setReviewedBy(req.getAdminId());
        app.setReviewedAt(new Date());
        app.setAdminNote(req.getNote());

        OwnerApplication saved = ownerApplicationRepository.save(app);

        User user = userRepository.findByUserId(app.getUserId())
                .orElseThrow(() -> new RuntimeException("Linked user not found for this application"));

        user.setRole("USER");
        userRepository.save(user);

        return saved;
    }

    public String removeOwner(String userId) {

        User user = userRepository.findByUserId(userId).orElseThrow(()-> new RuntimeException("User not found"));

        user.setRole("CUSTOMER");

        long deleted = ownerApplicationRepository.deleteByUserId(userId);
        if(deleted==0) throw new RuntimeException("Application not found");

        long deletedSalon = salonRepository.deleteBySalonOwnerId(userId);

        userRepository.save(user);
        return deleted + " record deleted";
    }

    public Salon verifySalon(String salonId, String adminId, String note) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole()))
            throw new RuntimeException("User is not admin");

        salon.setVerified(true);
        salon.setVerifiedByAdminId(adminId);
        salon.setAdminNote(note);

        return salonRepository.save(salon);
    }

    public List<Salon> getUnverifiedSalons() {
        return salonRepository.findByIsVerifiedFalse();
    }
}
