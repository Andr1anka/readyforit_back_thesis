package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String message;
    private String firstName;
    private String lastName;
    private Role role;
}
