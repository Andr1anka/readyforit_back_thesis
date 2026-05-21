package com.andr1anka.readyforit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * LiqPay використовує SHA1(privateKey + data + privateKey) у Base64 як підпис.
 * Це визначено документацією LiqPay.
 */
@Service
public class LiqPaySignatureService {

    @Value("${liqpay.private-key}")
    private String privateKey;

    /** Створює підпис для outgoing-запиту (Checkout). */
    public String sign(String base64Data) {
        return computeSignature(privateKey + base64Data + privateKey);
    }

    /** Перевіряє підпис callback'а від LiqPay. */
    public boolean verify(String base64Data, String receivedSignature) {
        String expected = sign(base64Data);
        return constantTimeEquals(expected, receivedSignature);
    }

    private String computeSignature(String input) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute LiqPay signature", e);
        }
    }

    /** Порівняння рядків без витоку часу (захист від timing attack). */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}