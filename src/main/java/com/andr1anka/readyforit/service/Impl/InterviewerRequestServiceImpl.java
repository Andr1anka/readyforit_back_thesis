package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.InterviewerApplicationRequestDTO;
import com.andr1anka.readyforit.dto.InterviewerApplicationResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.InterviewerRequest;
import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.InterviewerRequestRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.FileStorageService;
import com.andr1anka.readyforit.service.InterviewerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewerRequestServiceImpl implements InterviewerRequestService {

    private final UserRepository userRepository;
    private final InterviewerRequestRepository requestRepo;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public InterviewerApplicationResponseDTO apply(String email,
                                                   InterviewerApplicationRequestDTO dto,
                                                   List<MultipartFile> proofs) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (!user.isVerificated()) {
            throw new BadRequestException("Щоб подати заявку на інтерв'юера, спочатку пройдіть верифікацію");
        }
        if (user.getRole() == Role.INTERVIEWER) {
            throw new BadRequestException("Ви вже є інтерв'юером");
        }
        if (user.getInterviewerRequestStatus() == InterviewerRequestStatus.PENDING) {
            throw new BadRequestException("Ваша заявка вже на розгляді");
        }
        if (proofs == null || proofs.isEmpty()) {
            throw new BadRequestException("Додайте хоча б один документ як доказ досвіду");
        }

        // Зберігаємо файли (шифровані)
        List<String> keys = new ArrayList<>();
        for (MultipartFile f : proofs) {
            keys.add(fileStorageService.uploadInterviewerProof(f, user.getId()));
        }

        InterviewerRequest request = InterviewerRequest.builder()
                .user(user)
                .experienceDescription(dto.getExperienceDescription())
                .specialization(dto.getSpecialization())
                .yearsOfExperience(dto.getYearsOfExperience())
                .externalLinks(dto.getExternalLinks())
                .proofObjectKeys(keys)
                .status(InterviewerRequestStatus.PENDING)
                .build();
        requestRepo.save(request);

        user.setInterviewerRequestStatus(InterviewerRequestStatus.PENDING);
        user.setRole(Role.REQUEST_FOR_INTERVIEWER);
        userRepository.save(user);

        return toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewerApplicationResponseDTO getMyRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        return requestRepo.findTopByUserOrderByCreatedAtDesc(user)
                .map(this::toDto)
                .orElse(null);
    }

    private InterviewerApplicationResponseDTO toDto(InterviewerRequest r) {
        return InterviewerApplicationResponseDTO.builder()
                .id(r.getId())
                .status(r.getStatus())
                .experienceDescription(r.getExperienceDescription())
                .specialization(r.getSpecialization())
                .yearsOfExperience(r.getYearsOfExperience())
                .externalLinks(r.getExternalLinks())
                .proofsCount(r.getProofObjectKeys() == null ? 0 : r.getProofObjectKeys().size())
                .adminComment(r.getAdminComment())
                .createdAt(r.getCreatedAt())
                .reviewedAt(r.getReviewedAt())
                .build();
    }
}