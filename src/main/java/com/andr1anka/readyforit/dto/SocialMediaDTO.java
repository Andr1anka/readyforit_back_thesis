package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaDTO {
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String link;
}