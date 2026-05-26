package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Повна інформація для сторінки деталей заняття (Feature 3).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDetailsDTO {
    private Long lessonTypeId;
    private Long interviewerId;

    // інтерв'юер
    private String interviewerFirstName;
    private String interviewerLastName;
    private String interviewerPhoto;
    private Double interviewerRank;       // середній рейтинг
    private long reviewsCount;
    private boolean verified;
    private Integer experienceYears;

    // заняття
    private String title;
    private String shortDescription;
    private String longDescription;       // Markdown
    private List<String> tags;
    private int price;
    private Double durationMultiplier;
    private int effectiveDurationMinutes; // base × multiplier

    // слоти інтерв'юера (фіолетові вільні / сірі зайняті)
    private List<TimeSlotDTO> slots;
}
