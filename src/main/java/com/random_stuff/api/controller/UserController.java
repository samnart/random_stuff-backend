package com.random_stuff.api.controller;
 
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.dto.UserDto;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
 
    private final UserService userService;
 
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)));
    }
 
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
        @AuthenticationPrincipal User user,
        @RequestBody Map<String, String> updates
    ) {
        UserDto updatedUser = userService.updateUser(
            user.getId(),
            updates.get("name"),
            updates.get("phone")
        );
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updatedUser));
    }
 
    @GetMapping("/me/wishlist")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getWishlist(@AuthenticationPrincipal User user) {
        List<ProductDto> wishlist = userService.getWishlist(user.getId()).stream()
            .map(ProductDto::fromEntity)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }
 
    @PostMapping("/me/wishlist/{productId}")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
        @AuthenticationPrincipal User user,
        @PathVariable String productId
    ) {
        userService.addToWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", null));
    }
 
    @DeleteMapping("/me/wishlist/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
        @AuthenticationPrincipal User user,
        @PathVariable String productId
    ) {
        userService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }
}