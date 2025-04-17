package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User getOrCreateUserFromGoogle(OAuth2AccessToken accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken.getTokenValue();

        logger.debug("Google API URL: " + userInfoUrl);
        logger.debug("Access Token: " + accessToken.getTokenValue());

        OAuth2User googleUser = restTemplate.getForObject(userInfoUrl, OAuth2User.class);

        if (googleUser == null) {
            throw new RuntimeException("Could not retrieve user info from Google.");
        }

        String email = googleUser.getAttribute("email");
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            logger.debug("Existing user found: " + existingUser.getUsername());
            return existingUser;
        }

        User newUser = new User();
        newUser.setUsername(googleUser.getAttribute("name"));
        newUser.setEmail(email);
        newUser.setAvatar(googleUser.getAttribute("picture"));
        newUser.setRole("USER");

        User savedUser = userRepository.save(newUser);
        logger.debug("New user created: " + savedUser.getUsername());
        return savedUser;
    }

    public User saveUser(User user) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        // Kiểm tra mật khẩu có được cung cấp không
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        // Đặt avatar mặc định nếu không được cung cấp
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("https://t4.ftcdn.net/jpg/02/15/84/43/360_F_215844325_ttX9YiIIyeaR7Ne6EaLLjMAmy4GvPC69.jpg");
        }
        // Kiểm tra và đặt role
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        } else {
            // Đảm bảo role chỉ là USER, ADMIN hoặc EMPLOYEE
            String role = user.getRole().toUpperCase();
            if (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
                throw new RuntimeException("Invalid role. Role must be USER, ADMIN, or EMPLOYEE");
            }
            user.setRole(role);
        }
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        // Nếu mật khẩu được cung cấp, mã hóa lại
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // Kiểm tra role khi cập nhật
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            String role = user.getRole().toUpperCase();
            if (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
                throw new RuntimeException("Invalid role. Role must be USER, ADMIN, or EMPLOYEE");
            }
            user.setRole(role);
        }
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    public User addUser(User user) {
        return saveUser(user);
    }

    public User updateUser(Long userId, User user) {
        if (userRepository.existsById(userId)) {
            user.setId(userId);
            return updateUser(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserId(long userId) {
        return userRepository.findById(userId);
    }
}