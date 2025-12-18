package com.random_stuff.api.dto;
 
import com.random_stuff.api.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private String id;
    private String slug;
    private String name;
    private String icon;
    private int count;
 
    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
            .id(category.getId())
            .slug(category.getSlug())
            .name(category.getName())
            .icon(category.getIcon())
            .count(category.getProductCount())
            .build();
    }
}