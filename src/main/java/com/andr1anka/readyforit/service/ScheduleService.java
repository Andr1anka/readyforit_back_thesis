package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.LessonJoinInfoDTO;
import com.andr1anka.readyforit.dto.ScheduleResponseDTO;

public interface ScheduleService {

    /** Актуальні заняття користувача (майбутні, активні). */
    ScheduleResponseDTO getActual(String email);

    /** Архівні заняття користувача (минулі / завершені / скасовані). */
    ScheduleResponseDTO getArchived(String email);
    /** Інформація для приєднання до відеозвʼязку (з перевіркою, що користувач — учасник). */
    LessonJoinInfoDTO getJoinInfo(String email, Long lessonId);

}
