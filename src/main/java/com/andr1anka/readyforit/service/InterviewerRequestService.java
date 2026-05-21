package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.InterviewerApplicationRequestDTO;
import com.andr1anka.readyforit.dto.InterviewerApplicationResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InterviewerRequestService {

    InterviewerApplicationResponseDTO apply(
            String email,
            InterviewerApplicationRequestDTO dto,
            List<MultipartFile> proofs
    );

    InterviewerApplicationResponseDTO getMyRequest(String email);
}