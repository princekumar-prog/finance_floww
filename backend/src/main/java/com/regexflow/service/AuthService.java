package com.regexflow.service;

import com.regexflow.dto.AuthRequest;
import com.regexflow.dto.AuthResponse;
import com.regexflow.dto.RegisterRequest;
import com.regexflow.entity.User;
import com.regexflow.exception.DuplicateResourceException;
import com.regexflow.repository.UserRepository;
import com.regexflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .role(request.getRole())
            .active(true)
            .build();
        
        userRepository.save(user);
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String token = jwtUtil.generateToken(userDetails, claims);
        
        log.info("User registered successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .message("Registration successful")
            .build();
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // Load user details
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String token = jwtUtil.generateToken(userDetails, claims);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .message("Login successful")
            .build();
    }
}
