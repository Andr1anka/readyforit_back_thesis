package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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
}
