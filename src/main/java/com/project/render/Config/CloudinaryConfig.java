package com.project.render.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dwf0dlwoa",
                "api_key", "912611554888811",
                "api_secret", "jPL7GiAn4_LAleH8-whvz8aCe4o",
                "secure", true
        ));
    }
}
