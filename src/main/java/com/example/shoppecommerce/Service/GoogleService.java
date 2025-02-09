package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
public class GoogleService {
    @Autowired
    UserRepository userRepository;

    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public OAuth2AccessToken getAccessTokenFromGoogle(String token) {
        // Chỉ cần tạo OAuth2AccessToken từ token truyền vào
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, null, null);
    }

    public User getOrCreateUserFromGoogle(OAuth2AccessToken accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken.getTokenValue();

        System.out.println("Calling Google API: " + userInfoUrl);
        System.out.println("Access Token: " + accessToken.getTokenValue());

        Map<String, Object> googleUserInfo = restTemplate.getForObject(userInfoUrl, Map.class);

        if (googleUserInfo == null || !googleUserInfo.containsKey("email")) {
            System.err.println("ERROR: Could not retrieve user info from Google API!");
            throw new RuntimeException("Could not retrieve user info from Google.");
        }

        System.out.println("Google User Info: " + googleUserInfo); // Debug

        String email = (String) googleUserInfo.get("email");
        String name = (String) googleUserInfo.get("name");
        String picture = (String) googleUserInfo.get("picture");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Tạo user mới nếu chưa tồn tại
        User newUser = new User();
        newUser.setUsername(email);
        newUser.setEmail(email);
        newUser.setAvatar(picture);
        newUser.setRole("USER");

        return userRepository.save(newUser);
    }

}
