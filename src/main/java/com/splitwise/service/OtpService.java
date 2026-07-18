package com.splitwise.service;

import com.splitwise.model.OtpEntity;
import com.splitwise.model.User;
import com.splitwise.repository.OtpRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 10;

    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpEntity.setUsed(false);
        otpRepository.save(otpEntity);

        emailService.sendOtpMail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntity otpEntity = otpRepository
                .findTopByEmailAndOtpAndIsUsedFalseOrderByCreatedAtDesc(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        return true;
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        OtpEntity otpEntity = otpRepository
                .findTopByEmailAndOtpOrderByCreatedAtDesc(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        if (!otpEntity.isUsed()) {
            throw new RuntimeException("OTP not verified. Please verify OTP first.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRepository.delete(otpEntity);
    }
}