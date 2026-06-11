package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.PaymentHistoryItemDTO;
import com.andr1anka.readyforit.dto.TopupRequestDTO;
import com.andr1anka.readyforit.dto.TopupResponseDTO;
import com.andr1anka.readyforit.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/topup/init")
    public ResponseEntity<TopupResponseDTO> initTopup(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody TopupRequestDTO dto
    ) {
        return ResponseEntity.ok(paymentService.initTopup(principal.getUsername(), dto));
    }


    @PostMapping("/liqpay/callback")
    public ResponseEntity<Void> liqpayCallback(
            @RequestParam("data") String data,
            @RequestParam("signature") String signature
    ) {
        paymentService.handleCallback(data, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryItemDTO>> getMyHistory(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.ok(paymentService.getMyHistory(principal.getUsername()));
    }


    @PostMapping("/topup/confirm")
    public ResponseEntity<PaymentHistoryItemDTO> confirmTopup(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("orderId") String orderId
    ) {
        return ResponseEntity.ok(paymentService.confirmTopup(principal.getUsername(), orderId));
    }


    @PostMapping("/topup/confirm-pending")
    public ResponseEntity<List<PaymentHistoryItemDTO>> confirmPendingTopups(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.ok(paymentService.confirmPendingTopups(principal.getUsername()));
    }
}