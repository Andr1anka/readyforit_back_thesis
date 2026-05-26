package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.service.InterviewerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Самообслуговування інтерв'юера: налаштування, види занять, слоти.
 * Усі ендпоінти вимагають автентифікації (роль INTERVIEWER перевіряється у сервісі).
 */
@RestController
@RequestMapping("/api/interviewer/profile")
@RequiredArgsConstructor
public class InterviewerProfileController {

    private final InterviewerProfileService service;

    // ---- Налаштування ----
    @GetMapping("/settings")
    public ResponseEntity<InterviewerSettingsDTO> getSettings(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.getMySettings(principal.getUsername()));
    }

    @PutMapping("/settings")
    public ResponseEntity<InterviewerSettingsDTO> updateSettings(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody InterviewerSettingsDTO dto) {
        return ResponseEntity.ok(service.updateMySettings(principal.getUsername(), dto));
    }

    // ---- Види занять ----
    @GetMapping("/lesson-types")
    public ResponseEntity<List<LessonTypeResponseDTO>> getLessonTypes(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.getMyLessonTypes(principal.getUsername()));
    }

    @PostMapping("/lesson-types")
    public ResponseEntity<LessonTypeResponseDTO> createLessonType(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody LessonTypeRequestDTO dto) {
        return ResponseEntity.ok(service.createLessonType(principal.getUsername(), dto));
    }

    @PutMapping("/lesson-types/{id}")
    public ResponseEntity<LessonTypeResponseDTO> updateLessonType(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody LessonTypeRequestDTO dto) {
        return ResponseEntity.ok(service.updateLessonType(principal.getUsername(), id, dto));
    }

    @DeleteMapping("/lesson-types/{id}")
    public ResponseEntity<Void> deleteLessonType(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        service.deleteLessonType(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // ---- Слоти ----
    @PostMapping("/slots/preview")
    public ResponseEntity<List<TimeSlotDTO>> previewSlots(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody GenerateSlotsRequestDTO dto) {
        return ResponseEntity.ok(service.previewSlots(principal.getUsername(), dto));
    }

    @PostMapping("/slots")
    public ResponseEntity<List<TimeSlotDTO>> saveSlots(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody List<TimeSlotDTO> slots) {
        return ResponseEntity.ok(service.saveSlots(principal.getUsername(), slots));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotDTO>> getSlots(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.getMySlots(principal.getUsername()));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<Void> deleteSlot(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        service.deleteSlot(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
