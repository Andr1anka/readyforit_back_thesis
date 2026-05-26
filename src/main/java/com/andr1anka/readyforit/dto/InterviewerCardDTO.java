package com.andr1anka.readyforit.dto;

import jakarta.persistence.GeneratedValue;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InterviewerCardDTO {
    private Long id;                  // id виду заняття (InformationAboutLesson)
    private Long interviewerId;       // id інтерв'юера (для переходу в деталі/розклад)

    private String name;
    private String lastName;
    private String photo;
    private Double rank;

    private boolean isVerified;
    private List<String> tags = new ArrayList<String>();

    //lesson title
    private String title ;
    private String shortDescription;
    private int price;
    private Integer effectiveDurationMinutes;

    private Integer experienceYears;  // років досвіду (з заявки інтерв'юера)
    private String format;            // "Онлайн" — наразі всі заняття онлайн
}
