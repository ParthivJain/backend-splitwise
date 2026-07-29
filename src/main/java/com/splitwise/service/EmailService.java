package com.splitwise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_USERNAME}")
    private String fromEmail;

    public void sendOtpMail(String to, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("\uD83D\uDD10 Password Reset OTP - SplitWise");
        message.setText(
                "Hello,\n\n" +
                        "You requested to reset your password.\n" +
                        "Your OTP is : " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "Thanks,\nSplitWise Team"
        );
        mailSender.send(message);
    }

}
