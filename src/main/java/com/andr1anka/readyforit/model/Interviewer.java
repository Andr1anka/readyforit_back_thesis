package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interviewer")
public class Interviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "is_verificated")
//    private boolean isVerificated;

    @NotNull
    @Column(name = "planned_session_duration_minutes", nullable = false)
    private Integer plannedSessionDurationMinutes;

    @Column(name = "expected_time_for_break")
    private Integer expectedTimeForBreak;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy ="interviewer",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)

    private List<SocialMedia> socialMediaList = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy ="interviewer",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Availability> availabilityList = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy ="interviewer",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<InformationAboutLesson> informationAboutLessons = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy ="interviewer",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Lesson> scheduledLessons = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "interviewer",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<TimeSlots> timeSlots = new ArrayList<>();

}
