package com.project.render.Controller;

import com.project.render.Entity.Auth;
import com.project.render.IO.ProfileRequest;
import com.project.render.IO.ProfileResponse;
import com.project.render.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1.0")
public class UserController {

    @Autowired
    private UserService profileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        return profileService.registerUser(request);
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody Auth request) {
        if(request.getEmail()==null || request.getOtp()==null){
            throw new IllegalArgumentException("Email and Otp are required");
        }
        return profileService.verifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/login")
    public ProfileResponse login(@RequestBody Auth request) {
        if(request.getEmail()==null || request.getPassword()==null){
            throw new IllegalArgumentException("Email and password are required");
        }
        return profileService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestBody Auth request) {
        if(request.getEmail()==null){
            throw new IllegalArgumentException("Email is required");
        }
        return profileService.resendOtp(request.getEmail());
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Auth request) {
        return profileService.forgotPassword(request.getEmail());
    }

    @PostMapping("/verify-reset-otp")
    public String verifyResetOtp(@RequestBody Auth request) {
        return profileService.verifyResetOtp(request.getEmail(),request.getOtp());
    }

    @PostMapping("/new-password")
    public String newPassword(@RequestBody Auth request) {
        return profileService.newPassword(request.getEmail(),request.getPassword());
    }
}
