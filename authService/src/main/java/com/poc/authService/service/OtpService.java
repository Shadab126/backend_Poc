package com.poc.authService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class OtpService {

    private final Map<String, String> otpStore = new HashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new HashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        otpStore.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        log.info("OTP for {} = {}", email, otp);

        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        if (!otpStore.containsKey(email)) return false;

        if (otpExpiry.get(email).isBefore(LocalDateTime.now()))
            return false; // expired

        return otpStore.get(email).equals(otp);
    }
}
