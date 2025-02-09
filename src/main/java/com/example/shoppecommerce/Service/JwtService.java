package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JwtService {

    private static final Logger logger = Logger.getLogger(JwtService.class.getName());

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        String privateKeyPath = "src/main/resources/private_key.pem";
        String publicKeyPath = "src/main/resources/public_key.pem";

        // Kiểm tra file có tồn tại không
        if (!Files.exists(Paths.get(privateKeyPath)) || !Files.exists(Paths.get(publicKeyPath))) {
            throw new RuntimeException("ERROR: JWT Key files not found! Check private_key.pem and public_key.pem.");
        }

        // Đọc Private Key
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Đọc Public Key
        String publicKeyContent = new String(Files.readAllBytes(Paths.get(publicKeyPath)))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);

        System.out.println("✅ Private/Public Key loaded successfully.");
    }



    // Tạo JWT Token
    public String generateToken(UserDetails userDetails, Long userId, String role) {
        Map<String, Object> claims = Map.of(
                "username", userDetails.getUsername(),
                "userId", userId,
                "role", role
        );
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return createToken(Map.of("username", userDetails.getUsername()), userDetails.getUsername(), refreshExpiration);
    }

    // Tạo JWT Token chung
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256) // Dùng PrivateKey để ký
                .compact();

        System.out.println("🛑 Generated JWT Token: " + token);
        return token;
    }



    // Trích xuất thông tin từ Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Xác minh JWT bằng Public Key
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // Dùng Public Key để xác minh token
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long getExpiresIn() {
        return jwtExpiration;
    }

    // Sinh JWT từ đối tượng User
    public String generateTokenFromUser(User user) {
        Map<String, Object> claims = Map.of(
                "username", user.getUsername(),
                "userId", user.getId(),
                "role", user.getRole()
        );
        return createToken(claims, user.getUsername(), jwtExpiration);
    }
}
