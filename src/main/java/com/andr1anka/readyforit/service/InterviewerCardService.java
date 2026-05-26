package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.dto.InterviewerFilterDTO;
import com.andr1anka.readyforit.dto.PagedResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface InterviewerCardService {
    List<InterviewerCardDTO> getAllCards();

    /** Список карток з фільтрацією, сортуванням і пагінацією. */
    PagedResponseDTO<InterviewerCardDTO> getCards(InterviewerFilterDTO filter);

    /** Усі унікальні мітки (для фільтра збоку). */
    List<String> getAllTags();
}
