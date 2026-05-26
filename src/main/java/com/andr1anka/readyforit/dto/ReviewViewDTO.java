package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.ReviewType;
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
public class ReviewViewDTO {
    private Long id;
    private Long lessonId;
    private String lessonTitle;

    // інша сторона відгуку (для "мої" — про кого; для "отримані" — від кого)
    private String counterpartFirstName;
    private String counterpartLastName;
    private String counterpartPhoto;

    private Integer rating;
    private String comment;
    private ReviewType reviewType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
