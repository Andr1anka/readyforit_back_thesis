package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "attachments")
@Data
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    @NotBlank
    @Column(name = "file_name")
    private String fileName;

    @NotBlank
    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Complaint  complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Lesson  lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Review review;

    @PrePersist
    @PreUpdate
    private void validate() {
        long count = 0;
        if (complaint != null) count++;
        if (lesson != null) count++;
        if (review != null) count++;

        if (count != 1) {
            throw new IllegalStateException("Attachment must belong to exactly one of: Complaint, Lesson or Review");
        }
    }

}
