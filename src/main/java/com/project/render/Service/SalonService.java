package com.project.render.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.render.DTO.SalonCardResponse;
import com.project.render.DTO.SalonDetails;
import com.project.render.DTO.ServiceResponse;
import com.project.render.Entity.DocumentType;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceCategoryRepository;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SalonService {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    private String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "auto")
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    public Salon addSalon(
            String ownerId,
            String name,
            String city,
            String address,
            String contact,
            String salonEmail,
            String opentimeStr,
            String closetimeStr,
            String mapLink,

            MultipartFile cover,
            MultipartFile interior,
            MultipartFile exterior,
            MultipartFile ownerPhoto,

            DocumentType documentType,
            MultipartFile document
    ) {

        Optional<User> ownerOpt = userRepository.findByUserId(ownerId);

        if (ownerOpt.isEmpty())
            throw new IllegalArgumentException("Owner not found");

        User owner = ownerOpt.get();

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new IllegalArgumentException("User is not owner");

        LocalTime open = LocalTime.parse(opentimeStr);
        LocalTime close = LocalTime.parse(closetimeStr);

        if (open.isAfter(close) || open.equals(close))
            throw new IllegalArgumentException("Invalid salon timing");

        String base = "salons/" + ownerId;

        String coverUrl = upload(cover, base + "/cover");
        String interiorUrl = upload(interior, base + "/interior");
        String exteriorUrl = upload(exterior, base + "/exterior");
        String ownerUrl = upload(ownerPhoto, base + "/owner");
        String docUrl = upload(document, base + "/document");

        if (docUrl != null && documentType == null)
            throw new IllegalArgumentException("Document type required");

        Salon salon = new Salon();

        salon.setSalonOwnerId(ownerId);
        salon.setName(name);
        salon.setCity(city);
        salon.setAddress(address);
        salon.setContact(contact);
        salon.setSalonEmail(salonEmail);
        salon.setOpentime(open);
        salon.setClosetime(close);
        salon.setMapLink(mapLink);

        salon.setImageUrl(coverUrl);
        salon.setInteriorImageUrl(interiorUrl);
        salon.setExteriorImageUrl(exteriorUrl);
        salon.setOwnerPhotoUrl(ownerUrl);

        salon.setDocumentType(documentType);
        salon.setDocumentUrl(docUrl);

        return salonRepository.save(salon);
    }

    public List<SalonCardResponse> getAllSalonWithServices(){

        List<Salon> salons = salonRepository.findByIsVerifiedTrue();
        List<SalonCardResponse> response = new ArrayList<>();


        for(Salon salon : salons) {

            List<String> serviceNames = new ArrayList<>();

            if(salon.getServiceIds()!=null){
                salon.getServiceIds().forEach(id -> {
                    serviceCategoryRepository.findById(id).ifPresent
                            (service -> serviceNames.add(service.getName()));
                });
            }

            response.add(
                    new SalonCardResponse(
                            salon.getId(),
                            salon.getName(),
                            salon.getCity(),
                            salon.getImageUrl(),
                            serviceNames
                    )
            );
        }

        return response;

    }

    public SalonDetails getSalonDetails(String salonId){

        Salon salon = salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException());

        List<ServiceResponse> services = new ArrayList<>();

        if(salon.getServiceIds()!=null){
            salon.getServiceIds().forEach(id -> {
                serviceCategoryRepository.findById(id).ifPresent
                        (service ->
                                services.add(
                                        new ServiceResponse(
                                        service.getId(),
                                        service.getName(),
                                        service.getDescription()
                                        )
                                )
                        );
            });
        }

       return new SalonDetails(
                salon.getId(),
                salon.getName(),
                salon.getAddress(),
                salon.getCity(),
                salon.getImageUrl(),
                salon.getContact(),
                salon.getSalonEmail(),
                services
        );

    }

    public Salon updateSalonPartial(
            String salonId,
            String ownerId,

            String name,
            String city,
            String address,
            String contact,
            String salonEmail,
            String opentimeStr,
            String closetimeStr,
            String mapLink,

            MultipartFile cover
    ) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (!salon.getSalonOwnerId().equals(ownerId))
            throw new IllegalArgumentException("You are not allowed to update this salon");

        if (name != null) salon.setName(name);
        if (city != null) salon.setCity(city);
        if (address != null) salon.setAddress(address);
        if (contact != null) salon.setContact(contact);
        if (salonEmail != null) salon.setSalonEmail(salonEmail);
        if (mapLink != null) salon.setMapLink(mapLink);

        if (opentimeStr != null || closetimeStr != null) {
            LocalTime open = (opentimeStr != null) ? LocalTime.parse(opentimeStr) : salon.getOpentime();
            LocalTime close = (closetimeStr != null) ? LocalTime.parse(closetimeStr) : salon.getClosetime();

            if (open.isAfter(close) || open.equals(close))
                throw new IllegalArgumentException("Invalid salon timing");

            salon.setOpentime(open);
            salon.setClosetime(close);
        }

        if (cover != null && !cover.isEmpty()) {
            String base = "salons/" + ownerId;
            String coverUrl = upload(cover, base + "/cover");
            salon.setImageUrl(coverUrl);
        }

        return salonRepository.save(salon);
    }

    public void deleteSalon(String salonId, String ownerId) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (!salon.getSalonOwnerId().equals(ownerId))
            throw new IllegalArgumentException("You are not allowed to delete this salon");

        Optional<User> ownerOpt = userRepository.findByUserId(ownerId);

        if (ownerOpt.isEmpty())
            throw new IllegalArgumentException("Owner not found");

        User owner = ownerOpt.get();

        if (!"OWNER".equalsIgnoreCase(owner.getRole()))
            throw new IllegalArgumentException("User is not owner");

        salonRepository.deleteById(salonId);
    }

    public List<Salon> getAllSalon(){
        return salonRepository.findAll();
    }

    public Salon getSalon(String salonId){
        return salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found"));
    }

    public List<Salon> getSalonByOwnrId(String ownerId) {
        return salonRepository.findBySalonOwnerId(ownerId);
    }
}
