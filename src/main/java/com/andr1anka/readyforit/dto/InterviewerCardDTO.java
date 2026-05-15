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
    private Long id;

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


}
