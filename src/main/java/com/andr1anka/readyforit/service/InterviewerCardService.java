package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface InterviewerCardService {
    List<InterviewerCardDTO> getAllCards();
}
