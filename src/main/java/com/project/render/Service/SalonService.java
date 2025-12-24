package com.project.render.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.render.DTO.SalonCardResponse;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.ServiceRepository;
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
    private ServiceRepository serviceRepository;

    public Salon addSalon(String ownerId, String name, String city, String address,
                          String opentimeStr, String closetimeStr, MultipartFile image) {

        Optional<User> isOwner = userRepository.findByUserId(ownerId);
        if(isOwner.isEmpty()){
            throw new IllegalArgumentException("Owner not found");
        }

        User owner = isOwner.get();

        if (!"OWNER".equalsIgnoreCase(owner.getRole())) {
            throw new IllegalArgumentException("User is not an owner");
        }

        LocalTime open = LocalTime.parse(opentimeStr);
        LocalTime close = LocalTime.parse(closetimeStr);
        if (open.isAfter(close)) {
            throw new IllegalArgumentException("Invalid open and close times");
        }

        Salon salon = new Salon();
        salon.setSalonOwnerId(ownerId);
        salon.setName(name);
        salon.setCity(city);
        salon.setAddress(address);
        salon.setOpentime(open);
        salon.setClosetime(close);

        if (image != null && !image.isEmpty()) {

            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                        ObjectUtils.asMap("folder", "salons"));
                String imageUrl = uploadResult.get("secure_url").toString();
                salon.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image to Cloudinary", e);
            }

        }

        return salonRepository.save(salon);

    }

    public List<SalonCardResponse> getAllSalonWithServices(){

        List<Salon> salons = salonRepository.findAll();
        List<SalonCardResponse> response = new ArrayList<>();


        for(Salon salon : salons) {

            List<String> serviceNames = new ArrayList<>();

            if(salon.getServiceIds()!=null){
                salon.getServiceIds().forEach(id -> {
                    serviceRepository.findById(id).ifPresent
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

    public List<Salon> getAllSalon(){
        return salonRepository.findAll();
    }

    public Salon getSalon(String salonId){
        return salonRepository.findById(salonId).orElseThrow(()-> new RuntimeException("Salon not found"));
    }

}
