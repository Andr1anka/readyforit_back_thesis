package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    @NotNull
    private Long lessonId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 1500)
    private String comment;
}
