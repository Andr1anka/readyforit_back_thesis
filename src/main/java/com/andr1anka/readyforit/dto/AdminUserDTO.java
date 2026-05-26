package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean blocked;
    private boolean verificated;
    private VerificationStatus verificationStatus;
    private Double rank;

    // Дані останньої ручної KYC-верифікації для вкладки адміністратора
    private Long verificationRequestId;
    private String profilePhotoUrl;
    private String documentUrl;
    private String selfieUrl;
    private Boolean nameMatch;
    private Double faceSimilarity;
    private Boolean faceMatch;
    private String extractedTextPreview;
    private LocalDateTime verificationCreatedAt;
}
