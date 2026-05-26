package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.BookingRequestDTO;
import com.andr1anka.readyforit.dto.BookingResponseDTO;
import com.andr1anka.readyforit.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> book(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.ok(bookingService.book(principal.getUsername(), request));
    }
}
