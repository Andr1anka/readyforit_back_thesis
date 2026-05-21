package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerApplicationRequestDTO {

    @NotBlank
    @Size(min = 30, max = 4000, message = "Опис має бути від 30 до 4000 символів")
    private String experienceDescription;

    @NotBlank
    private String specialization;

    @Min(0)
    private Integer yearsOfExperience;

    private String externalLinks; // optional
}