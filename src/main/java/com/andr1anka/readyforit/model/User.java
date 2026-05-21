package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.validation.constraints.Email;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table (name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "picture")
    private String picture;

    @NotBlank
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Column(name = "last_name")
    private String lastName;
    private Integer age;

    @Column(name = "email",unique = true)
    @Email
    @NotBlank
    private String email;

    @Column(name = "password")
    @NotBlank
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "enabled")
    private boolean enabled = false;   // чи підтвердив email

    @Column (name = "rank")
    private Double rank;

    @Column(name = "is_verificated")
    private boolean isVerificated;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.NONE;

    @Column(name = "interviewer_request_status")
    @Enumerated(EnumType.STRING)
    private InterviewerRequestStatus interviewerRequestStatus = InterviewerRequestStatus.NONE;


    @Column(name = "balance")
    private BigDecimal balance;

    @OneToMany(mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Complaint> complaint= new ArrayList<>();

    @OneToOne(mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Interviewer interviewer;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy ="user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Lesson> scheduledLessons = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<EmailVerificationToken> verificationTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();
}
