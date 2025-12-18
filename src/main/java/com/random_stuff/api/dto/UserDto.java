package com.random_stuff.api.dto;
 
import com.random_stuff.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String avatar;
    private String phone;
 
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .phone(user.getPhone())
            .build();
    }
}