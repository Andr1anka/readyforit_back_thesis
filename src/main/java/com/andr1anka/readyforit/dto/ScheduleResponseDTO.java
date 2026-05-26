package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO {
    private List<ScheduleItemDTO> items;
    /** "list" якщо актуальних < 5, "calendar" якщо >= 5 (підказка для фронту). */
    private String suggestedView;
    private int actualCount;
}
