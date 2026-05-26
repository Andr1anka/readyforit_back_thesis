package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.LessonDetailsDTO;
import com.andr1anka.readyforit.dto.PagedResponseDTO;
import com.andr1anka.readyforit.dto.ReviewItemDTO;

/**
 * Деталі заняття (Feature 3): повна інформація, слоти, відгуки.
 */
public interface LessonDetailsService {

    /** Повна інформація про конкретний вид заняття + слоти інтерв'юера. */
    LessonDetailsDTO getLessonDetails(Long lessonTypeId);

    /** Відгуки про інтерв'юера з пагінацією та сортуванням. */
    PagedResponseDTO<ReviewItemDTO> getInterviewerReviews(Long interviewerId, int page, int size, String sort);
}
