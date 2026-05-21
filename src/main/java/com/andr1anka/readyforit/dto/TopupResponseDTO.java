package com.andr1anka.readyforit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupResponseDTO {
    private String orderId;
    private String data;        // Base64 JSON для LiqPay
    private String signature;   // HMAC SHA1 (Base64)
    private String checkoutUrl; // https://www.liqpay.ua/api/3/checkout
}