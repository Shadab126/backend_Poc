package com.poc.authService.controller;

import com.poc.authService.entity.Role;
import com.poc.authService.entity.User;
import com.poc.authService.repository.UserRepository;
import com.poc.authService.service.CustomUserDetailsService;
import com.poc.authService.service.JwtService;
import com.poc.authService.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final CustomUserDetailsService uds;
    private final JwtService jwt;
    private final MailService mailService;

    // Temporary OTP store
    private final Map<String, String> otpStore = new HashMap<>();

    // Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (repo.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email is already exist"));
        }

        if (repo.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username is already exists"));
        }
        // pass bcrypt
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User req) {

        User user = repo.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect Password"));
        }

        UserDetails details = uds.loadUserByUsername(user.getUsername());
        String token = jwt.generateToken(details);

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "role", user.getRole().name(),
                        "message", "Login successful"
                )
        );
    }

    // Change password

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String header,
            @RequestBody Map<String, String> req) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Missing token"));
        }

        String token = header.substring(7);
        String username = jwt.extractUsername(token);

        String currentPass = req.get("currentPassword");
        String newPass = req.get("newPassword");
        String confirmPass = req.get("confirmPassword");

        User user = repo.findByUsername(username).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));

        if (!encoder.matches(currentPass, user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Current password is incorrect"));

        if (!newPass.equals(confirmPass))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Passwords do not match"));

        user.setPassword(encoder.encode(newPass));
        repo.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully"));
    }


    // FORGOT PASSWORD - SEND OTP
    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> req) {

        String email = req.get("email");

        User user = repo.findByEmail(email).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Email not found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStore.put(email, otp);

        mailService.sendOtp(email, otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // VERIFY OTP + RESET PASSWORD
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String otp = req.get("otp");
        String newPass = req.get("newPassword");
        String confirmPass = req.get("confirmPassword");

        if (!otpStore.containsKey(email))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "OTP not generated"));

        if (!otpStore.get(email).equals(otp))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid OTP"));

        if (!newPass.equals(confirmPass))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Passwords do not match"));

        User user = repo.findByEmail(email).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));

        user.setPassword(encoder.encode(newPass));
        repo.save(user);
        otpStore.remove(email);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // for api gateway
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody String token) {
        try {
            jwt.extractUsername(token); // if invalid → exception thrown
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }

    @GetMapping("/extract-username")
    public ResponseEntity<String> extractUsername(@RequestParam String token) {
        String username = jwt.extractUsername(token);
        return ResponseEntity.ok(username);
    }

    @GetMapping("/test")
    public String test() {
        return "Auth Service OK";
    }

    // Get All Users for Admin only
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {

        var users = repo.findByRole(Role.USER).stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole() != null ? user.getRole().name() : null);
                    return map;
                })
                .toList();

        return ResponseEntity.ok(users);
    }

}