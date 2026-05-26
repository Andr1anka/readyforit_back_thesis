package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonTypeResponseDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String longDescription;
    private List<String> tags;
    private Integer price;
    private Double durationMultiplier;
    /** base × multiplier — фактична тривалість слота цього заняття у хвилинах. */
    private Integer effectiveDurationMinutes;
}
