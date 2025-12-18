package com.random_stuff.api.service;

import com.random_stuff.api.dto.AuthRequest;
import com.random_stuff.api.dto.AuthResponse;
import com.random_stuff.api.dto.UserDto;
import com.random_stuff.api.entity.PasswordResetToken;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.PasswordResetTokenRepository;
import com.random_stuff.api.repository.UserRepository;
import com.random_stuff.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
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
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, UserDto.fromEntity(user));
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // Generate a new reset token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS))
            .build();

        passwordResetTokenRepository.save(resetToken);

        // In production, you would send this via email
        // For development/testing, we log it and return it
        log.info("Password reset token generated for {}: {}", user.getEmail(), token);

        // Return the token for testing purposes
        // In production, you'd return a generic success message and send email
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
