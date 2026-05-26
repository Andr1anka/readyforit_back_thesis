package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.LessonJoinInfoDTO;
import com.andr1anka.readyforit.dto.ScheduleResponseDTO;
import com.andr1anka.readyforit.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/actual")
    public ResponseEntity<ScheduleResponseDTO> actual(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(scheduleService.getActual(principal.getUsername()));
    }

    @GetMapping("/archived")
    public ResponseEntity<ScheduleResponseDTO> archived(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(scheduleService.getArchived(principal.getUsername()));
    }

    @GetMapping("/join/{lessonId}")
    public ResponseEntity<LessonJoinInfoDTO> joinInfo(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(scheduleService.getJoinInfo(principal.getUsername(), lessonId));
    }
}
