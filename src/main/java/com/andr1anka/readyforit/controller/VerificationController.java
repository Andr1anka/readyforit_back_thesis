package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.VerificationResponseDTO;
import com.andr1anka.readyforit.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VerificationResponseDTO> submit(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("document") MultipartFile document,
            @RequestPart("selfie") MultipartFile selfie
    ) {
        return ResponseEntity.ok(verificationService.submit(principal.getUsername(), document, selfie));
    }

    @GetMapping("/status")
    public ResponseEntity<VerificationResponseDTO> getMyStatus(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(verificationService.getMyStatus(principal.getUsername()));
    }

    @PostMapping("/escalate")
    public ResponseEntity<Void> escalate(@AuthenticationPrincipal UserDetails principal) {
        verificationService.escalateToAdmin(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}