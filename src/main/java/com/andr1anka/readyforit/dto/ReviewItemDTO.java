package com.andr1anka.readyforit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDTO {
    private Long id;
    private String reviewerFirstName;
    private String reviewerLastName;
    private String reviewerPhoto;
    private Integer rating;       // 1-5
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
