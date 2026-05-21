package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.VerificationResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.model.VerificationRequest;
import com.andr1anka.readyforit.model.VerificationStatus;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.repository.VerificationRequestRepository;
import com.andr1anka.readyforit.service.FileStorageService;
import com.andr1anka.readyforit.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRepo;
    private final FileStorageService fileStorageService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kyc.python-service-url}")
    private String pythonServiceUrl;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public VerificationResponseDTO submit(String email, MultipartFile documentFront, MultipartFile liveSelfie) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (user.isVerificated()) {
            throw new BadRequestException("Ви вже верифіковані");
        }

        // 1. Зберігаємо файли в MinIO (шифровані)
        String docKey = fileStorageService.uploadKycDocument(documentFront, user.getId(), "doc");
        String selfieKey = fileStorageService.uploadKycDocument(liveSelfie, user.getId(), "selfie");

        // 2. Викликаємо Python KYC сервіс
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        Map<String, Object> kycResponse;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("document_front", toResource(documentFront));
            body.add("live_selfie", toResource(liveSelfie));
            body.add("user_full_name", fullName);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            kycResponse = restTemplate.postForObject(pythonServiceUrl + "/verify", entity, Map.class);
            if (kycResponse == null) throw new BadRequestException("Порожня відповідь від KYC-сервісу");
        } catch (Exception e) {
            log.error("KYC service call failed", e);
            // Збережемо запис як REJECTED з причиною помилки, але повернемо повідомлення
            VerificationRequest failed = VerificationRequest.builder()
                    .user(user)
                    .documentObjectKey(docKey)
                    .selfieObjectKey(selfieKey)
                    .status(VerificationStatus.REJECTED)
                    .adminComment("KYC-сервіс недоступний: " + e.getMessage())
                    .build();
            verificationRepo.save(failed);

            user.setVerificationStatus(VerificationStatus.REJECTED);
            userRepository.save(user);

            return VerificationResponseDTO.builder()
                    .status(VerificationStatus.REJECTED)
                    .message("Сервіс верифікації тимчасово недоступний. Ви можете передати на ручний розгляд адміну.")
                    .build();
        }

        // 3. Розбираємо результат
        String decision = (String) kycResponse.get("decision");
        Map<String, Object> details = (Map<String, Object>) kycResponse.get("details");

        Boolean nameMatch = (Boolean) details.get("name_match");
        Double faceSimilarity = ((Number) details.get("face_similarity")).doubleValue();
        Boolean faceMatch = (Boolean) details.get("face_match");
        String preview = (String) details.get("extracted_text_preview");

        VerificationStatus status = "VERIFIED".equals(decision)
                ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED;

        // 4. Запис у БД
        VerificationRequest request = VerificationRequest.builder()
                .user(user)
                .documentObjectKey(docKey)
                .selfieObjectKey(selfieKey)
                .nameMatch(nameMatch)
                .faceSimilarity(faceSimilarity)
                .faceMatch(faceMatch)
                .extractedTextPreview(preview)
                .status(status)
                .build();
        verificationRepo.save(request);

        // 5. Оновлюємо користувача
        user.setVerificationStatus(status);
        user.setVerificated(status == VerificationStatus.VERIFIED);
        userRepository.save(user);

        return VerificationResponseDTO.builder()
                .status(status)
                .nameMatch(nameMatch)
                .faceSimilarity(faceSimilarity)
                .faceMatch(faceMatch)
                .extractedTextPreview(preview)
                .message(status == VerificationStatus.VERIFIED
                        ? "Верифікацію успішно пройдено!"
                        : "Верифікація не пройдена. Ви можете передати справу адміністратору на ручний розгляд.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationResponseDTO getMyStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        return verificationRepo.findTopByUserOrderByCreatedAtDesc(user)
                .map(r -> VerificationResponseDTO.builder()
                        .status(r.getStatus())
                        .nameMatch(r.getNameMatch())
                        .faceSimilarity(r.getFaceSimilarity())
                        .faceMatch(r.getFaceMatch())
                        .extractedTextPreview(r.getExtractedTextPreview())
                        .message(r.getAdminComment())
                        .build())
                .orElse(VerificationResponseDTO.builder()
                        .status(VerificationStatus.NONE)
                        .build());
    }

    @Override
    @Transactional
    public void escalateToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        VerificationRequest last = verificationRepo.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("Спочатку пройдіть автоматичну верифікацію"));

        if (last.getStatus() != VerificationStatus.REJECTED) {
            throw new BadRequestException("Можна передати на розгляд лише після відхилення");
        }

        last.setStatus(VerificationStatus.ESCALATED);
        verificationRepo.save(last);

        user.setVerificationStatus(VerificationStatus.ESCALATED);
        userRepository.save(user);
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (Exception e) {
            throw new IllegalStateException("Не вдалося прочитати файл", e);
        }
    }
}