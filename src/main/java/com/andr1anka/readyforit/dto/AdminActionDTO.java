package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Універсальне тіло для дій адміна: рішення (approve/reject/resolve) + коментар. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionDTO {
    private String comment;
}
