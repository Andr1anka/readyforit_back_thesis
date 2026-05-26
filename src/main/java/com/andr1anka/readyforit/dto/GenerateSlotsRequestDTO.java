package com.andr1anka.readyforit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Інтерв'юер задає дату і бажаний проміжок (з/до). Бекенд нарізає його на слоти
 * по (тривалість заняття + перерва) і повертає пропоновані слоти, які інтерв'юер
 * може переглянути й видалити зайві перед підтвердженням.
 *
 * lessonTypeId опційний: якщо вказано — слоти нарізаються під тривалість саме
 * цього виду заняття (base × multiplier). Якщо null — під базову тривалість.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSlotsRequestDTO {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime from;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime to;

    private Long lessonTypeId;
}
