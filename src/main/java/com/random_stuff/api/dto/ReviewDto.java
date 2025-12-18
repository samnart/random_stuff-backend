package com.random_stuff.api.dto;
 
import com.random_stuff.api.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.format.DateTimeFormatter;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private String id;
    private String productId;
    private String userName;
    private int rating;
    private String date;
    private String title;
    private String comment;
    private int helpful;
    private boolean verified;
 
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
 
    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
            .id(review.getId())
            .productId(review.getProduct() != null ? review.getProduct().getId() : null)
            .userName(review.getUser() != null ? review.getUser().getName() : "Anonymous")
            .rating(review.getRating())
            .date(review.getCreatedAt() != null ? review.getCreatedAt().format(DATE_FORMAT) : null)
            .title(review.getTitle())
            .comment(review.getComment())
            .helpful(review.getHelpful())
            .verified(review.isVerified())
            .build();
    }
}