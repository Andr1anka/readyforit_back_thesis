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
public class PagedResponseDTO<T> {
    private List<T> content;
    private int page;          // поточна сторінка (0-based)
    private int size;          // розмір сторінки
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
