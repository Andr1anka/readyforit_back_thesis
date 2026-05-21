package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.InterviewerApplicationRequestDTO;
import com.andr1anka.readyforit.dto.InterviewerApplicationResponseDTO;
import com.andr1anka.readyforit.service.InterviewerRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/interviewer-request")
@RequiredArgsConstructor
public class InterviewerRequestController {

    private final InterviewerRequestService interviewerRequestService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterviewerApplicationResponseDTO> apply(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "proofs") List<MultipartFile> proofs
    ) throws Exception {
        InterviewerApplicationRequestDTO dto = objectMapper.readValue(dataJson, InterviewerApplicationRequestDTO.class);
        return ResponseEntity.ok(interviewerRequestService.apply(principal.getUsername(), dto, proofs));
    }

    @GetMapping("/me")
    public ResponseEntity<InterviewerApplicationResponseDTO> getMyRequest(
            @AuthenticationPrincipal UserDetails principal
    ) {
        InterviewerApplicationResponseDTO dto = interviewerRequestService.getMyRequest(principal.getUsername());
        return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
    }
}