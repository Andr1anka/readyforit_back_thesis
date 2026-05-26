package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequestDTO {
    @NotNull
    private Long lessonId;

    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;
}
