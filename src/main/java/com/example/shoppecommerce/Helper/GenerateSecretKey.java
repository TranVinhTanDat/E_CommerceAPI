package com.example.shoppecommerce.Helper;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;

public class GenerateSecretKey {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Thay đổi HS256 nếu bạn sử dụng thuật toán khác
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Secret Key: " + encodedKey);
    }
}
