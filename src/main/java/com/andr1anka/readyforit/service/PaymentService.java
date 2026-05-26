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

    /**
     * Підтвердити поповнення після повернення користувача з LiqPay.
     * Потрібно для локальної розробки / тестового режиму, де LiqPay
     * не може достукатись до server_url на localhost. Статус звіряється
     * напряму через LiqPay status API (а не з даних клієнта).
     */
    PaymentHistoryItemDTO confirmTopup(String email, String orderId);

    /**
     * Перевіряє всі незавершені транзакції користувача через LiqPay status API
     * і зараховує баланс для оплачених. Використовується при поверненні з LiqPay
     * коли orderId з sessionStorage міг бути втрачений.
     */
    List<PaymentHistoryItemDTO> confirmPendingTopups(String email);

    /** Історія платежів користувача. */
    List<PaymentHistoryItemDTO> getMyHistory(String email);
}