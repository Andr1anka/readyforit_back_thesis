package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Attachment;
import com.andr1anka.readyforit.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
}


