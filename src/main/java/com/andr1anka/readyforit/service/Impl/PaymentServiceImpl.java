package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.PaymentHistoryItemDTO;
import com.andr1anka.readyforit.dto.TopupRequestDTO;
import com.andr1anka.readyforit.dto.TopupResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.PaymentStatus;
import com.andr1anka.readyforit.model.PaymentTransaction;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.PaymentTransactionRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.LiqPaySignatureService;
import com.andr1anka.readyforit.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String LIQPAY_CHECKOUT_URL = "https://www.liqpay.ua/api/3/checkout";

    private final UserRepository userRepository;
    private final PaymentTransactionRepository txRepo;
    private final LiqPaySignatureService signatureService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${liqpay.public-key}")
    private String publicKey;

    @Value("${liqpay.sandbox}")
    private int sandbox; // 1 — sandbox, 0 — production

    @Value("${liqpay.result-url}")
    private String resultUrl;

    @Value("${liqpay.server-url}")
    private String serverUrl; // куди LiqPay присилає callback

    @Override
    @Transactional
    public TopupResponseDTO initTopup(String email, TopupRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Сума має бути додатна");
        }

        String orderId = "rfi-" + UUID.randomUUID();

        // Запис у БД ДО запиту, щоб callback мав з чим звіритись
        PaymentTransaction tx = PaymentTransaction.builder()
                .user(user)
                .orderId(orderId)
                .amount(dto.getAmount())
                .currency(dto.getCurrency() == null ? "UAH" : dto.getCurrency())
                .status(sandbox == 1 ? PaymentStatus.SANDBOX : PaymentStatus.CREATED)
                .description("Поповнення балансу ReadyForIt")
                .build();
        txRepo.save(tx);

        // Будуємо payload для LiqPay
        Map<String, Object> params = new HashMap<>();
        params.put("public_key", publicKey);
        params.put("version", "3");
        params.put("action", "pay");
        params.put("amount", dto.getAmount().toPlainString());
        params.put("currency", tx.getCurrency());
        params.put("description", tx.getDescription());
        params.put("order_id", orderId);
        params.put("sandbox", sandbox);
        if (resultUrl != null && !resultUrl.isBlank()) params.put("result_url", resultUrl);
        if (serverUrl != null && !serverUrl.isBlank()) params.put("server_url", serverUrl);

        String jsonStr;
        try {
            jsonStr = objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize LiqPay payload", e);
        }
        String data = Base64.getEncoder().encodeToString(jsonStr.getBytes(StandardCharsets.UTF_8));
        String signature = signatureService.sign(data);

        return TopupResponseDTO.builder()
                .orderId(orderId)
                .data(data)
                .signature(signature)
                .checkoutUrl(LIQPAY_CHECKOUT_URL)
                .build();
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handleCallback(String data, String signature) {
        // 1. Перевірка підпису — критично, інакше будь-хто може накрутити баланс
        if (!signatureService.verify(data, signature)) {
            log.warn("LiqPay callback with INVALID signature");
            throw new BadRequestException("Invalid signature");
        }

        // 2. Декодуємо
        String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid LiqPay payload");
        }

        String orderId = (String) payload.get("order_id");
        String status = (String) payload.get("status");
        Object amountObj = payload.get("amount");
        String currency = (String) payload.get("currency");
        String paymentId = String.valueOf(payload.getOrDefault("payment_id", ""));

        // ⚠ безпечне витягування last4 з потенційно null значень
        Object senderCardMask2Obj = payload.get("sender_card_mask2");
        String senderCardMask2 = senderCardMask2Obj == null ? null : senderCardMask2Obj.toString();
        String cardLast4 = extractLast4(senderCardMask2);
        Object cardTypeObj = payload.get("sender_card_type");
        String cardType = cardTypeObj == null ? null : cardTypeObj.toString();

        PaymentTransaction tx = txRepo.findByOrderId(orderId)
                .orElseThrow(() -> new BadRequestException("Транзакцію не знайдено: " + orderId));

        // Захист від подвійного зарахування (idempotency)
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Duplicate callback for already successful order {}", orderId);
            return;
        }

        BigDecimal callbackAmount = new BigDecimal(amountObj.toString());
        // Перевірка суми — щоб не зарахувати інше значення
        if (callbackAmount.compareTo(tx.getAmount()) != 0) {
            log.warn("Amount mismatch for order {}: expected {}, got {}",
                    orderId, tx.getAmount(), callbackAmount);
            tx.setStatus(PaymentStatus.FAILURE);
            txRepo.save(tx);
            return;
        }

        // Перевірка валюти
        if (!tx.getCurrency().equalsIgnoreCase(currency)) {
            log.warn("Currency mismatch for order {}", orderId);
            tx.setStatus(PaymentStatus.FAILURE);
            txRepo.save(tx);
            return;
        }

        boolean success = "success".equalsIgnoreCase(status)
                || "sandbox".equalsIgnoreCase(status)
                || "wait_compensation".equalsIgnoreCase(status);

        tx.setLiqpayPaymentId(paymentId);
        tx.setCardLast4(cardLast4);
        tx.setCardType(cardType);
        tx.setCompletedAt(LocalDateTime.now());

        if (success) {
            tx.setStatus("sandbox".equalsIgnoreCase(status) ? PaymentStatus.SANDBOX : PaymentStatus.SUCCESS);

            User user = tx.getUser();
            BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
            user.setBalance(current.add(tx.getAmount()));
            userRepository.save(user);
        } else if ("reversed".equalsIgnoreCase(status)) {
            tx.setStatus(PaymentStatus.REVERSED);
        } else {
            tx.setStatus(PaymentStatus.FAILURE);
        }

        txRepo.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryItemDTO> getMyHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        return txRepo.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(t -> PaymentHistoryItemDTO.builder()
                        .orderId(t.getOrderId())
                        .amount(t.getAmount())
                        .currency(t.getCurrency())
                        .status(t.getStatus())
                        .cardLast4(t.getCardLast4())
                        .cardType(t.getCardType())
                        .createdAt(t.getCreatedAt())
                        .completedAt(t.getCompletedAt())
                        .build())
                .toList();
    }

    /** З маски '4242****4242' беремо останні 4 цифри. */
    private String extractLast4(String mask) {
        if (mask == null || mask.length() < 4) return null;
        String last4 = mask.substring(mask.length() - 4);
        return last4.matches("\\d{4}") ? last4 : null;
    }
}