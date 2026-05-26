package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.LessonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Один урок у розкладі (актуальний або архівний).
 * counterpart* — інша сторона: для студента це інтерв'юер, для інтерв'юера — студент.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDTO {
    private Long lessonId;
    private String title;

    private String counterpartFirstName;
    private String counterpartLastName;
    private String counterpartPhoto;
    private String role;            // "STUDENT" або "INTERVIEWER" — ким є поточний користувач у цьому уроці

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeOfLesson;

    private Integer durationMinutes;
    private Integer price;
    private LessonStatus status;
    private String link;            // кімната для відеозвʼязку

    // для архівних — рецензія від інтерв'юера + чи поточний користувач уже залишив відгук
    private String reviewFromInterviewer;
    private Integer myRating;       // оцінка, яку поточний користувач поставив (якщо є)
    private String myReviewComment;
    private boolean canReview;      // чи можна залишити відгук (урок завершено й ще не залишено)
}
