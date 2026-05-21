package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviewer_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Опис досвіду від користувача
    @Column(name = "experience_description", length = 4000, nullable = false)
    private String experienceDescription;

    // Спеціалізація / напрям
    @Column(name = "specialization")
    private String specialization;

    // Роки досвіду
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    // Посилання на докази (сертифікати, LinkedIn, GitHub тощо)
    @Column(name = "external_links", length = 2000)
    private String externalLinks;

    // Ключі завантажених файлів (PDF, скріни сертифікатів)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "interviewer_request_proofs",
            joinColumns = @JoinColumn(name = "request_id")
    )
    @Column(name = "object_key")
    @Builder.Default
    private List<String> proofObjectKeys = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewerRequestStatus status;

    @Column(name = "admin_comment", length = 1000)
    private String adminComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}