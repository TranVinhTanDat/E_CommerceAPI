package com.example.shoppecommerce.Config;

import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint công khai
                        .requestMatchers("/", "/auth/**", "/login", "/oauth2/**").permitAll()
                        .requestMatchers("/categories/**", "/products/**").permitAll()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/messages/**", "/ws/**").permitAll()
                        // Các endpoint yêu cầu xác thực
                        .requestMatchers("/cart/**", "/orders/user/**", "/addresses/**", "/comments/**").authenticated()
                        // Các endpoint yêu cầu quyền ADMIN
                        .requestMatchers("/admin/users/**", "/admin/products/**", "/admin/dashboard").hasRole("ADMIN")
                        // Các endpoint cho cả ADMIN và EMPLOYEE
                        .requestMatchers("/admin/orderList", "/admin/shipperOrderList", "/admin/chat").hasAnyRole("ADMIN", "EMPLOYEE")
                        // Tất cả các request khác cần xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(oauth2FailureHandler())
                );

        return http.build();
    }
    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            // Xử lý sau khi đăng nhập thành công: redirect hoặc trả về JSON
            response.sendRedirect("/auth/oauth2/success");
        };
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, exception) -> {
            // Xử lý khi đăng nhập thất bại
            response.sendRedirect("/auth/oauth2/failure");
        };
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("https://e-commerce-fe-seven-snowy.vercel.app"); // Origin của frontend
        configuration.addAllowedOrigin("http://localhost:3000"); // Cho phép localhost khi dev
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}