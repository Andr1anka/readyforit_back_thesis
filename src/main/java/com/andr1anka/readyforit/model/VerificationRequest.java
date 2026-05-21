package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ключі файлів у MinIO (bucket kyc-documents) — зашифровані
    @Column(name = "document_object_key", nullable = false)
    private String documentObjectKey;

    @Column(name = "selfie_object_key", nullable = false)
    private String selfieObjectKey;

    // Результат від Python-сервісу
    @Column(name = "name_match")
    private Boolean nameMatch;

    @Column(name = "face_similarity")
    private Double faceSimilarity;

    @Column(name = "face_match")
    private Boolean faceMatch;

    @Column(name = "extracted_text_preview", length = 1000)
    private String extractedTextPreview;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VerificationStatus status;

    @Column(name = "admin_comment", length = 1000)
    private String adminComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}