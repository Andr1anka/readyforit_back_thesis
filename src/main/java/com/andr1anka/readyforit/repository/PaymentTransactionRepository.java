package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.PaymentTransaction;
import com.andr1anka.readyforit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderId(String orderId);
    List<PaymentTransaction> findAllByUserOrderByCreatedAtDesc(User user);
}