package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name ="complaint")
@Data
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "description",length = 1000)
    private String description;

    @CreatedDate
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ComplaintStatus status;

    /** Урок, у звʼязку з яким подано скаргу (скаргу можна подати лише за наявності уроку). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Lesson lesson;

    /** На кого скарга (інша сторона уроку). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accused_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User accusedUser;

    /** Коментар адміністратора при вирішенні. */
    @Column(name = "admin_comment", length = 1000)
    private String adminComment;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "complaint",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;
}
