package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.mapper.InterviewerCardMapper;
import com.andr1anka.readyforit.model.InformationAboutLesson;
import com.andr1anka.readyforit.repository.InformationAboutLessonRepository;
import com.andr1anka.readyforit.repository.LessonRepository;
import com.andr1anka.readyforit.service.InterviewerCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.andr1anka.readyforit.mapper.InterviewerCardMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewerCardServiceImpl implements InterviewerCardService {
    private final InformationAboutLessonRepository imformationAboutLessonRepository;
    private final InterviewerCardMapper interviewerCardMapper;

    @Autowired
    public InterviewerCardServiceImpl(InformationAboutLessonRepository imformationAboutLessonRepository, InterviewerCardMapper interviewerCardMapper) {
        this.imformationAboutLessonRepository = imformationAboutLessonRepository;
        this.interviewerCardMapper = interviewerCardMapper;
    }

    @Override
    public List<InterviewerCardDTO> getAllCards() {
        return imformationAboutLessonRepository.findAll()
                .stream()
                .map(interviewerCardMapper::toDTO)
                .collect(Collectors.toList());
    }
}
