package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewViewDTO> submitReview(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.submitReview(principal.getUsername(), request));
    }

    @PostMapping("/interviewer-feedback")
    public ResponseEntity<ScheduleItemDTO> submitInterviewerFeedback(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody InterviewerFeedbackRequestDTO request) {
        return ResponseEntity.ok(reviewService.submitInterviewerFeedback(principal.getUsername(), request));
    }

    @GetMapping("/written")
    public ResponseEntity<List<ReviewViewDTO>> myWritten(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(reviewService.getMyWritten(principal.getUsername()));
    }

    @GetMapping("/received")
    public ResponseEntity<List<ReviewViewDTO>> received(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(reviewService.getReceived(principal.getUsername()));
    }

    @PostMapping("/complaints")
    public ResponseEntity<ComplaintViewDTO> submitComplaint(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ComplaintRequestDTO request) {
        return ResponseEntity.ok(reviewService.submitComplaint(principal.getUsername(), request));
    }

    @GetMapping("/complaints/my")
    public ResponseEntity<List<ComplaintViewDTO>> myComplaints(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(reviewService.getMyComplaints(principal.getUsername()));
    }
}
