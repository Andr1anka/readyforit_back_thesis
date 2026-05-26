package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.LessonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long lessonId;
    private String lessonTitle;
    private String interviewerFirstName;
    private String interviewerLastName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime timeOfLesson;

    private Integer price;
    private Integer durationMinutes;
    private LessonStatus status;
    private String link;

    /** Новий баланс студента після списання. */
    private BigDecimal newBalance;
}
