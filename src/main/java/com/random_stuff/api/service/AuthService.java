package com.random_stuff.api.service;
 
import com.random_stuff.api.dto.AuthRequest;
import com.random_stuff.api.dto.AuthResponse;
import com.random_stuff.api.dto.UserDto;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.repository.UserRepository;
import com.random_stuff.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
public class AuthService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
 
    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
 
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
 
        user = userRepository.save(user);
 
        String token = jwtUtil.generateToken(user);
 
        return new AuthResponse(token, UserDto.fromEntity(user));
    }
 
    public AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
            )
        );
 
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        String token = jwtUtil.generateToken(user);
 
        return new AuthResponse(token, UserDto.fromEntity(user));
    }
 
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        // In a real app, generate a reset token, save it, and send email
        // For now, just log it
        System.out.println("Password reset requested for: " + user.getEmail());
    }
}