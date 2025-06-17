package com.example.digitalloanservice.security;

import com.example.digitalloanservice.model.UserEntity;
import com.example.digitalloanservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List; // Prefer List.of for immutable lists

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable @PreAuthorize etc. for method-level security
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository; // To load user from DB

    /**
     * Configures a UserDetailsService to load user details from the UserRepository.
     * This is crucial for Spring Security to find users during authentication and authorization.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            // Return a Spring Security UserDetails object.
            // Ensure roles are prefixed with "ROLE_" for hasRole() expressions.
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(), user.getPassword(), List.of(() -> "ROLE_" + user.getRole()));
        };
    }

    /**
     * Provides a PasswordEncoder bean, using BCrypt for strong password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the AuthenticationProvider which uses our custom UserDetailsService
     * and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Provides the AuthenticationManager for manual authentication (e.g., in AuthController).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the security filter chain, configuring authorization rules, session management,
     * and integrating our JWT filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/register", "/auth/login").permitAll() // Allow public access to auth endpoints
                        .requestMatchers("/loan/all").hasRole("ADMIN") // Only ADMIN can access all loans
                        .requestMatchers("/loan/{id}/approve", "/loan/{id}/reject").hasRole("ADMIN") // Only ADMIN can approve/reject loans
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions for JWT (no HttpSession)
                )
                .authenticationProvider(authenticationProvider()) // Set our custom authentication provider
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // Add our JWT filter before Spring's default username/password filter

        return http.build();
    }
}