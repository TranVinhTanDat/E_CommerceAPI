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


    // Cách tạo hoặc lấy người dùng từ Google
    public User getOrCreateUserFromGoogle(OAuth2AccessToken accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken.getTokenValue();

        logger.debug("Google API URL: " + userInfoUrl); // Ghi log URL Google API
        logger.debug("Access Token: " + accessToken.getTokenValue()); // Ghi log token

        OAuth2User googleUser = restTemplate.getForObject(userInfoUrl, OAuth2User.class);

        if (googleUser == null) {
            throw new RuntimeException("Could not retrieve user info from Google.");
        }

        String email = googleUser.getAttribute("email");
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            logger.debug("Existing user found: " + existingUser.getUsername()); // Ghi log người dùng đã có
            return existingUser;
        }

        // Tạo mới người dùng nếu chưa tồn tại
        User newUser = new User();
        newUser.setUsername(googleUser.getAttribute("name"));
        newUser.setEmail(email);
        newUser.setAvatar(googleUser.getAttribute("picture"));
        newUser.setRole("USER");

        User savedUser = userRepository.save(newUser);
        logger.debug("New user created: " + savedUser.getUsername()); // Ghi log người dùng mới
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
        // Đặt avatar mặc định nếu không được cung cấp
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("https://t4.ftcdn.net/jpg/02/15/84/43/360_F_215844325_ttX9YiIIyeaR7Ne6EaLLjMAmy4GvPC69.jpg");
        }
        // Đặt role mặc định nếu không được cung cấp
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");  // Giá trị mặc định là "USER"
        }
        // Mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        // Không kiểm tra sự tồn tại, vì đây là phương thức cập nhật
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
        return userRepository.save(user);
    }

    public User updateUser(Long userId, User user) {
        if (userRepository.existsById(userId)) {
            user.setId(userId);
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Xóa khách hàng
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
