package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.TokenModel;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.UserRepository;
import com.example.shoppecommerce.Service.EmailService;
import com.example.shoppecommerce.Service.GoogleService;
import com.example.shoppecommerce.Service.JwtService;
import com.example.shoppecommerce.Service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private UserRepository userRepository; // THÊM DÒNG NÀY

    @Autowired
    private GoogleService googleService;



    private final Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token received");
        }

        try {
            // Gửi token lên Google để xác thực
            RestTemplate restTemplate = new RestTemplate();
            String googleValidationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token.replace("Bearer ", "");
            Map<String, Object> googleUserData = restTemplate.getForObject(googleValidationUrl, Map.class);

            if (googleUserData == null || !googleUserData.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
            }

            // 🛠 Lấy thông tin từ Google
            String email = googleUserData.get("email").toString();
            String name = googleUserData.get("name").toString();
            String avatar = googleUserData.get("picture").toString();

            // 🔍 Kiểm tra xem người dùng đã tồn tại chưa
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                // Nếu user chưa tồn tại, tạo mới
                user = new User();
                user.setUsername(name);
                user.setEmail(email);
                user.setAvatar(avatar);
                user.setRole("USER");

                // ✅ Thêm mật khẩu mặc định
                user.setPassword(passwordEncoder.encode("google_auth_default_password"));

                userRepository.save(user);
            }

            // ✅ **Tạo JWT mới để dùng trong hệ thống của bạn**
            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String jwtToken = jwtService.generateToken(userDetails, user.getId(), user.getRole());
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            System.out.println("✅ Generated JWT Token : " + jwtToken);


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
    public TokenModel login(@RequestBody User user) {
        logger.info("Attempting to authenticate user: {}", user.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final User authenticatedUser = userService.findByUsername(user.getUsername());
        final String jwt = jwtService.generateToken(userDetails, authenticatedUser.getId(), authenticatedUser.getRole());
        final String refreshToken = jwtService.generateRefreshToken(userDetails);
        final Long expiresIn = jwtService.getExpiresIn();
//        logger.info("User authenticated successfully: {} (ID: {})", user.getUsername(), authenticatedUser.getId());
        return new TokenModel(jwt, refreshToken, expiresIn, authenticatedUser.getRole(),authenticatedUser.getId());
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

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store OTP in a HashMap (should use Redis or DB in production)
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

        // Kiểm tra OTP trong bộ nhớ
        if (otpStorage.containsValue(enteredOtp)) {
            // Xóa OTP sau khi xác minh thành công
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

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!newPassword.equals(confirmNewPassword)) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        // Kiểm tra OTP trong bộ nhớ
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
