package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.VerificationResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface VerificationService {

    VerificationResponseDTO submit(String email, MultipartFile documentFront, MultipartFile liveSelfie);

    VerificationResponseDTO getMyStatus(String email);

    void escalateToAdmin(String email);
}