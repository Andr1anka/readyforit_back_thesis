package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Параметри фільтрації/сортування/пагінації для списку інтерв'юерів.
 * Усі поля опційні.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerFilterDTO {
    private List<String> tags;     // спеціалізації (OR-матч)
    private Integer minPrice;
    private Integer maxPrice;
    private String search;         // пошук по імені/назві заняття

    /** Сортування: "default" | "price_asc" | "price_desc" | "rank_desc". */
    private String sort;

    private Integer page;          // 0-based, default 0
    private Integer size;          // default 6
}
