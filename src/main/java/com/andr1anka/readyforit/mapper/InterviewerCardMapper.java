package com.andr1anka.readyforit.mapper;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.model.InformationAboutLesson;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InterviewerCardMapper {
    public InterviewerCardDTO toDTO (InformationAboutLesson informationAboutLesson) {
       if (informationAboutLesson == null) return null;
       List<String> tags = new ArrayList<>();
       if (informationAboutLesson.getSpecializations() != null && !informationAboutLesson.getSpecializations().isEmpty()) {
           tags = Arrays.stream(informationAboutLesson.getSpecializations().split(","))
                   .map(String::trim)
                   .collect(Collectors.toList());
       }
       return InterviewerCardDTO.builder()
               .id(informationAboutLesson.getId())
               .name(informationAboutLesson.getInterviewer().getUser().getFirstName())
               .lastName(informationAboutLesson.getInterviewer().getUser().getLastName())
               .photo(informationAboutLesson.getInterviewer().getUser().getPicture())
               .rank(informationAboutLesson.getInterviewer().getUser().getRank())
               .isVerified(informationAboutLesson.getInterviewer().isVerificated())
               .tags(tags)
               .title(informationAboutLesson.getTitle())
               .shortDescription(informationAboutLesson.getShortDescription())
               .price(informationAboutLesson.getPrice())
               .build();
    }

}
