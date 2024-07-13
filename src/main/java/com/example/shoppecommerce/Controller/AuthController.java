    package com.example.shoppecommerce.Controller;

    import com.example.shoppecommerce.Entity.TokenModel;
    import com.example.shoppecommerce.Entity.User;
    import com.example.shoppecommerce.Service.JwtService;
    import com.example.shoppecommerce.Service.UserService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.web.bind.annotation.*;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

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

        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody User user) {
            try {
                userService.saveUser(user);
                return ResponseEntity.ok("User registered successfully");
            } catch (RuntimeException e) {
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
            final String jwt = jwtService.generateToken(userDetails);
            final String refreshToken = jwtService.generateRefreshToken(userDetails);
            final Long expiresIn = jwtService.getExpiresIn();

            logger.info("User authenticated successfully: {} (ID: {})", user.getUsername(), authenticatedUser.getId());
            return new TokenModel(jwt, refreshToken, expiresIn);
        }
    }

