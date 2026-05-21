package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    private Role role;
    private Double rank;
    private BigDecimal balance;
    private boolean hasCustomAvatar;        // true → avatar URL валідний; false → фронт малює SVG з ініціалами
    private String avatarUrl;               // /api/user/me/avatar (присутній лише якщо hasCustomAvatar)
    private String initials;                // для генерації SVG на фронті
    private boolean isVerificated;
    private VerificationStatus verificationStatus;
    private InterviewerRequestStatus interviewerRequestStatus;
}