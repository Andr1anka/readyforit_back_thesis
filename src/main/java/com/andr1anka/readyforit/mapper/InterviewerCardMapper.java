package com.andr1anka.readyforit.mapper;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.model.InformationAboutLesson;
import com.andr1anka.readyforit.model.Interviewer;
import com.andr1anka.readyforit.model.User;
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
                   .filter(t -> !t.isEmpty())
                   .collect(Collectors.toList());
       }
       Interviewer interviewer = informationAboutLesson.getInterviewer();
       User user = interviewer == null ? null : interviewer.getUser();
       int baseDuration = interviewer == null || interviewer.getPlannedSessionDurationMinutes() == null
               ? 60
               : interviewer.getPlannedSessionDurationMinutes();
       double multiplier = informationAboutLesson.getDurationMultiplier() == null
               ? 1.0
               : informationAboutLesson.getDurationMultiplier();
       String avatarUrl = user != null && user.getPicture() != null && !user.getPicture().isBlank()
               ? "/api/user/me/avatar?u=" + user.getId()
               : null;

       return InterviewerCardDTO.builder()
               .id(informationAboutLesson.getId())
               .interviewerId(interviewer == null ? null : interviewer.getId())
               .name(user == null ? null : user.getFirstName())
               .lastName(user == null ? null : user.getLastName())
               .photo(avatarUrl)
               .rank(user == null ? null : user.getRank())
               .isVerified(user != null && user.isVerificated())
               .tags(tags)
               .title(informationAboutLesson.getTitle())
               .shortDescription(informationAboutLesson.getShortDescription())
               .price(informationAboutLesson.getPrice())
               .effectiveDurationMinutes((int) Math.round(baseDuration * multiplier))
               .format("Онлайн")
               .build();
    }

}
