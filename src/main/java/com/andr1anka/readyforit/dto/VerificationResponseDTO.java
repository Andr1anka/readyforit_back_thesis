package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponseDTO {
    private VerificationStatus status;
    private Boolean nameMatch;
    private Double faceSimilarity;
    private Boolean faceMatch;
    private String extractedTextPreview;
    private String message;
}