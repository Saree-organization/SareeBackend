package com.web.saree.security;

import org.springframework.beans.factory.annotation.Autowired;
//import org.context.annotation.Bean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Make sure this is imported
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Make sure this is imported
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List; // Import List

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Keep CSRF disabled
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Use the bean below
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Keep OPTIONS preflight requests permitted

                        // General Public/User Endpoints (Remain the same)
                        .requestMatchers("/api/auth/", "/sarees/").permitAll()
                        .requestMatchers("/public/", "/images/").permitAll()
                        .requestMatchers("/api/contact/").permitAll()

                        // Standard User Endpoints
                        .requestMatchers("/api/wishlist/","/api/cart/").authenticated()
                        .requestMatchers("/api/payment/create-order", "/api/payment/verify", "/api/payment/orders", "/api/payment/cancel-order").authenticated()

                        // 🎯 FIX 1: Admin GET Endpoints - Require ADMIN role
                        // /admin-orders, /admin-all-orders, /admin/user-orders/{userId}, /admin/user/{userId}
                        .requestMatchers("/api/payment/admin-orders", "/api/payment/admin-all-orders", "/api/payment/admin/user-orders/", "/api/payment/admin/user/").hasRole("ADMIN")

                        // 🎯 FIX 2: Admin PUT/POST Endpoints - Require ADMIN role
                        // Status Change API: PUT admin/paymentChangeStatus/{orderIdentifier}/status
                        .requestMatchers(HttpMethod.PUT, "/admin/paymentChangeStatus/").hasRole("ADMIN")

                        // Mark Paid & Ship API: POST /api/payment/admin/mark-paid-and-ship/{orderId}
                        .requestMatchers(HttpMethod.POST, "/api/payment/admin/mark-paid-and-ship/").hasRole("ADMIN")

                        // All other requests must be authenticated (as a fallback)
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // This correctly allows all origins when using credentials
        config.addAllowedOriginPattern("*");

        config.setAllowedHeaders(List.of("")); // Use List.of("")
        config.setAllowedMethods(List.of("")); // Use List.of("")

        source.registerCorsConfiguration("/", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}