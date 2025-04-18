package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.TokenModel;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.UserRepository;
import com.example.shoppecommerce.Service.EmailService;
import com.example.shoppecommerce.Service.JwtService;
import com.example.shoppecommerce.Service.UserService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    private final Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token received");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String googleValidationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token.replace("Bearer ", "");
            Map<String, Object> googleUserData = restTemplate.getForObject(googleValidationUrl, Map.class);

            if (googleUserData == null || !googleUserData.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
            }

            String email = googleUserData.get("email").toString();
            String name = googleUserData.get("name").toString();
            String avatar = googleUserData.get("picture").toString();

            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                user = new User();
                user.setUsername(name);
                user.setEmail(email);
                user.setAvatar(avatar);
                user.setRole("USER");
                user.setPassword(passwordEncoder.encode("google_auth_default_password"));
                userRepository.save(user);
            }

            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String jwtToken = jwtService.generateToken(userDetails, user.getId(), user.getRole());
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new TokenModel(jwtToken, refreshToken, jwtService.getExpiresIn(), user.getRole(), user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("Error occurred while registering user", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        logger.info("Attempting to authenticate user: {}", user.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            final User authenticatedUser = userService.findByUsername(user.getUsername());
            final String jwt = jwtService.generateToken(userDetails, authenticatedUser.getId(), authenticatedUser.getRole());
            final String refreshToken = jwtService.generateRefreshToken(userDetails);
            final Long expiresIn = jwtService.getExpiresIn();
            return ResponseEntity.ok(new TokenModel(jwt, refreshToken, expiresIn, authenticatedUser.getRole(), authenticatedUser.getId()));
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.extractUsername(token.substring(7));
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-avatar")
    public ResponseEntity<?> updateAvatar(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.extractUsername(token.substring(7));
            User user = userService.findByUsername(username);
            if (user != null) {
                String newAvatar = payload.get("avatar");
                if (newAvatar == null || newAvatar.isEmpty()) {
                    return ResponseEntity.badRequest().body("Avatar URL is missing or empty");
                }
                user.setAvatar(newAvatar);
                userService.updateUser(user);
                return ResponseEntity.ok("Avatar updated successfully");
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.extractUsername(token.substring(7));
            User user = userService.findByUsername(username);

            if (!passwordEncoder.matches(payload.get("currentPassword"), user.getPassword())) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(payload.get("newPassword")));
            userService.updateUser(user);

            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);

        try {
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok("OTP has been sent to your email.");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Error sending email: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String enteredOtp = request.get("otp");

        if (otpStorage.containsValue(enteredOtp)) {
            otpStorage.entrySet().removeIf(entry -> entry.getValue().equals(enteredOtp));
            return ResponseEntity.ok("OTP verified successfully.");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP. Please try again.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        String confirmNewPassword = request.get("confirmNewPassword");

        if (!newPassword.equals(confirmNewPassword)) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        Optional<String> emailOptional = otpStorage.entrySet().stream()
                .filter(entry -> entry.getValue().equals(otp))
                .map(Map.Entry::getKey)
                .findFirst();

        if (emailOptional.isPresent()) {
            String email = emailOptional.get();
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok("Password has been reset successfully.");
            }
        }
        return ResponseEntity.badRequest().body("Invalid OTP or User not found.");
    }
}