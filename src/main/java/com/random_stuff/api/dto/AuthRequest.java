package com.random_stuff.api.dto;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
public class AuthRequest {
 
    @Data
    public static class Login {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
 
        @NotBlank(message = "Password is required")
        private String password;
    }
 
    @Data
    public static class Register {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;
 
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
 
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
    }
 
    @Data
    public static class ForgotPassword {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }
 
    @Data
    public static class ResetPassword {
        @NotBlank(message = "Token is required")
        private String token;
 
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPassword;
    }
}