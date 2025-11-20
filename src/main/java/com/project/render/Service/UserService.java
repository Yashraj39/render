package com.project.render.Service;

import com.project.render.Entity.User;
import com.project.render.IO.ProfileRequest;
import com.project.render.IO.ProfileResponse;
import com.project.render.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public ProfileResponse registerUser(ProfileRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
        }

        String generatedOtp = generateOtp();
        User newUser = User.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .verifyOtp(generatedOtp)
                .verifiedOtpExpireAt(System.currentTimeMillis() + 5*60*1000)
                .build();

        userRepository.save(newUser);

        emailService.sendOtpEmail(newUser.getEmail(), generatedOtp);

        return ProfileResponse.builder()
                .userId(newUser.getUserId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .isAccountVerified(newUser.getIsAccountVerified())
                .build();
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(user.getVerifyOtp() != null &&
                user.getVerifyOtp().equals(otp) &&
                System.currentTimeMillis() <= user.getVerifiedOtpExpireAt()) {

            user.setIsAccountVerified(true);
            user.setVerifyOtp(null);
            user.setVerifiedOtpExpireAt(0L);
            userRepository.save(user);

            return "OTP verified successfully. Please login.";
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
    }

    public ProfileResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(!user.getIsAccountVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified");
        }

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .isAccountVerified(user.getIsAccountVerified())
                .build();
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(user.getIsAccountVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already verified");
        }

        String otp = generateOtp();
        user.setVerifyOtp(otp);
        user.setVerifiedOtpExpireAt(System.currentTimeMillis() + 5*60*1000);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP resent to your email";
    }

    public String generateOtp() {
        int otp = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setResetOtpExpireAt(System.currentTimeMillis() + 5 * 60 * 1000);
        user.setIsResetOtpVerified(false);
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);

        return "Reset OTP sent to email";
    }

    public String verifyResetOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!otp.equals(user.getResetOtp()) ||
                System.currentTimeMillis() > user.getResetOtpExpireAt()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.setIsResetOtpVerified(true);
        userRepository.save(user);

        return "OTP Verified. You can now set new password.";
    }


    public String newPassword(String email,String newPassword){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getIsResetOtpVerified() == null || !user.getIsResetOtpVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reset OTP not verified");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpireAt(0L);
        user.setIsResetOtpVerified(false);
        userRepository.save(user);

        return "Password reset successful. You can now login.";

    }
}
