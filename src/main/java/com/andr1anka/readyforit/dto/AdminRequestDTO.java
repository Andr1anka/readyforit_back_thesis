package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequestDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String experienceDescription;
    private Integer yearsOfExperience;
    private String externalLinks;
    private List<String> proofObjectKeys;
    private InterviewerRequestStatus status;
    private String adminComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
