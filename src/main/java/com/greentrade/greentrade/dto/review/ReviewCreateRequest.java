package com.greentrade.greentrade.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Reviewer ID is required")
    private Long reviewerId;
    
    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private Integer score;
    
    @Size(max = 1000, message = "Comment cannot be longer than 1000 characters")
    private String comment;
}