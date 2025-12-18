package com.random_stuff.api.service;
 
import com.random_stuff.api.dto.UserDto;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.ProductRepository;
import com.random_stuff.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
 
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
 
    public User findById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
 
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
 
    @Transactional
    public UserDto updateUser(String userId, String name, String phone) {
        User user = findById(userId);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        return UserDto.fromEntity(userRepository.save(user));
    }
 
    @Transactional(readOnly = true)
    public List<Product> getWishlist(String userId) {
        User user = findById(userId);
        return user.getWishlist();
    }
 
    @Transactional
    public void addToWishlist(String userId, String productId) {
        User user = findById(userId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
 
        if (!user.getWishlist().contains(product)) {
            user.getWishlist().add(product);
            userRepository.save(user);
        }
    }
 
    @Transactional
    public void removeFromWishlist(String userId, String productId) {
        User user = findById(userId);
        user.getWishlist().removeIf(p -> p.getId().equals(productId));
        userRepository.save(user);
    }
}