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
public class LessonJoinInfoDTO {
    private Long lessonId;
    private String title;
    private String room;          // link уроку = ідентифікатор кімнати
    private String counterpartFirstName;
    private String counterpartLastName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeOfLesson;

    private boolean joinable;     // чи відкрите вікно приєднання (за 15 хв до — до кінця)
}
