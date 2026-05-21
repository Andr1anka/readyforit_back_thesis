package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerApplicationResponseDTO {
    private Long id;
    private InterviewerRequestStatus status;
    private String experienceDescription;
    private String specialization;
    private Integer yearsOfExperience;
    private String externalLinks;
    private Integer proofsCount;
    private String adminComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}