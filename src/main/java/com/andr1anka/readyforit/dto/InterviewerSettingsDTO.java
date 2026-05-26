package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Базові налаштування інтерв'юера: тривалість одного (базового) заняття
 * та перерва між заняттями. Вони використовуються при генерації слотів.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerSettingsDTO {

    @NotNull
    @Min(value = 15, message = "Мінімум 15 хвилин")
    @Max(value = 480, message = "Максимум 8 годин")
    private Integer plannedSessionDurationMinutes;

    @NotNull
    @Min(value = 0)
    @Max(value = 120)
    private Integer expectedTimeForBreak;
}
