package com.regexflow.service;

import com.regexflow.dto.AuthRequest;
import com.regexflow.dto.AuthResponse;
import com.regexflow.dto.RegisterRequest;
import com.regexflow.entity.User;
import com.regexflow.enums.UserRole;
import com.regexflow.exception.DuplicateResourceException;
import com.regexflow.repository.UserRepository;
import com.regexflow.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("testuser")
            .email("test@test.com")
            .password("password123")
            .fullName("Test User")
            .role(UserRole.MAKER)
            .build();
        
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@test.com")
            .password("encodedPassword")
            .fullName("Test User")
            .role(UserRole.MAKER)
            .active(true)
            .build();
    }
    
    @Test
    void testRegister_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class), any())).thenReturn("token123");
        
        AuthResponse response = authService.register(registerRequest);
        
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("token123", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testRegister_duplicateUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> 
            authService.register(registerRequest)
        );
    }
    
    @Test
    void testRegister_duplicateEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> 
            authService.register(registerRequest)
        );
    }
    
    @Test
    void testLogin_success() {
        AuthRequest authRequest = new AuthRequest("testuser", "password123");
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class), any())).thenReturn("token123");
        
        AuthResponse response = authService.login(authRequest);
        
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("token123", response.getToken());
        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
