package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    @NotBlank(message = "Ім'я обов'язкове")
    private String firstName;

    @NotBlank(message = "Прізвище обов'язкове")
    private String lastName;

    @Email(message = "Невірний формат email")
    @NotBlank(message = "Email обов'язковий")
    private String email;

    @NotBlank(message = "Пароль обов'язковий")
    @Size(min = 8, message = "Пароль має містити мінімум 8 символів")
    private String password;
}
