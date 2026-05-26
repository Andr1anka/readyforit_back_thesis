package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Запит на створення/оновлення виду заняття (InformationAboutLesson).
 * Спеціалізації (мітки) передаються списком — на бекенді склеюються через кому.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonTypeRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String shortDescription;

    /** Markdown, зібраний редактором на фронті. Рендериться в HTML при показі. */
    @Size(max = 4000)
    private String longDescription;

    /** Мітки/спеціалізації: Java, BackEnd, ... (мін. 1). */
    @NotNull
    @Size(min = 1, message = "Додайте хоча б одну мітку")
    private List<@NotBlank String> tags;

    @NotNull
    @Min(value = 1, message = "Ціна має бути додатна")
    private Integer price;

    /**
     * Множник тривалості відносно базової тривалості інтерв'юера.
     * 1.0 — звичайне, 2.0 — подвійне (x2), 0.5 — половинне (x0.5).
     */
    @NotNull
    @DecimalMin(value = "0.25")
    @DecimalMax(value = "5.0")
    private Double durationMultiplier;
}
