package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.PaymentHistoryItemDTO;
import com.andr1anka.readyforit.dto.TopupRequestDTO;
import com.andr1anka.readyforit.dto.TopupResponseDTO;

import java.util.List;

public interface PaymentService {

    /** Створити замовлення на поповнення → повернути дані для LiqPay Checkout. */
    TopupResponseDTO initTopup(String email, TopupRequestDTO dto);

    /** Обробити callback від LiqPay. */
    void handleCallback(String data, String signature);

    /** Історія платежів користувача. */
    List<PaymentHistoryItemDTO> getMyHistory(String email);
}