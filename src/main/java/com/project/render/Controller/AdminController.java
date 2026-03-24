package com.project.render.Controller;

import com.project.render.DTO.AdminLoginRequest;
import com.project.render.DTO.AdminLoginResponse;
import com.project.render.Service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "https://salon-frontend-vercel-project.vercel.app"})
public class AdminController {

    @Autowired
    private AdminAuthService authService;

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        return authService.login(request);
    }
}