package com.project.render.Controller;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final Cloudinary cloudinary;

    @PostMapping("/image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "user-uploads")
            );

            String imageUrl = uploadResult.get("secure_url").toString();
            return Map.of("imageUrl", imageUrl);

        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }
}
