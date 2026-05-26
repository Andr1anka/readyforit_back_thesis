package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.LessonDetailsDTO;
import com.andr1anka.readyforit.dto.PagedResponseDTO;
import com.andr1anka.readyforit.dto.ReviewItemDTO;
import com.andr1anka.readyforit.service.LessonDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonDetailsController {

    private final LessonDetailsService lessonDetailsService;

    /** Повна інформація про вид заняття + слоти. */
    @GetMapping("/{lessonTypeId}")
    public ResponseEntity<LessonDetailsDTO> getDetails(@PathVariable Long lessonTypeId) {
        return ResponseEntity.ok(lessonDetailsService.getLessonDetails(lessonTypeId));
    }

    /** Відгуки про інтерв'юера (пагінація + сортування). */
    @GetMapping("/interviewer/{interviewerId}/reviews")
    public ResponseEntity<PagedResponseDTO<ReviewItemDTO>> getReviews(
            @PathVariable Long interviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(
                lessonDetailsService.getInterviewerReviews(interviewerId, page, size, sort));
    }
}
