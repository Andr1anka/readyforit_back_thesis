package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerFeedbackRequestDTO {
    @NotNull
    private Long lessonId;

    @NotBlank
    @Size(min = 30, max = 3000, message = "Рецензія має містити від 30 до 3000 символів")
    private String feedback;
}
