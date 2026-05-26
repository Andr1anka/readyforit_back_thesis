package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.ComplaintStatus;
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
public class ComplaintViewDTO {
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private String accusedFirstName;
    private String accusedLastName;
    private String title;
    private String description;
    private ComplaintStatus status;
    private String adminComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
