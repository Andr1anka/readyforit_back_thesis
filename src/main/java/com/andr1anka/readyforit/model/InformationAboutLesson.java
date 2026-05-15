package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Data
@NoArgsConstructor  // Додай це для Hibernate
@AllArgsConstructor
@Table(name = "information_about_lesson")
public class InformationAboutLesson {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id ;

    @NotBlank
    @Column(name = "title")
    private String title ;

    @NotBlank
    @Column(name = "short_description")
    private String shortDescription ;

    @Column(name= "long_description", length = 1000)
    private String longDescription ;

    @NotBlank
    @Column(name = "specializations")
    private String specializations;

    @NotNull
    @Column(name = "price")
    private int price;

    @NotNull
    @Column(name = "duration_multiplier")
    private Double durationMultiplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "interviewer_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Interviewer interviewer;

}
