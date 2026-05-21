package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.VerificationRequest;
import com.andr1anka.readyforit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    Optional<VerificationRequest> findTopByUserOrderByCreatedAtDesc(User user);
    List<VerificationRequest> findAllByUserOrderByCreatedAtDesc(User user);
}