package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.*;

import java.util.List;

/**
 * Самообслуговування інтерв'юера: базові налаштування, види занять (мітки/опис/ціна)
 * та генерація/керування слотами доступності.
 */
public interface InterviewerProfileService {

    /** Поточні налаштування інтерв'юера (тривалість + перерва). Створює Interviewer за потреби. */
    InterviewerSettingsDTO getMySettings(String email);

    InterviewerSettingsDTO updateMySettings(String email, InterviewerSettingsDTO dto);

    // ---- Види занять ----
    List<LessonTypeResponseDTO> getMyLessonTypes(String email);

    LessonTypeResponseDTO createLessonType(String email, LessonTypeRequestDTO dto);

    LessonTypeResponseDTO updateLessonType(String email, Long lessonTypeId, LessonTypeRequestDTO dto);

    void deleteLessonType(String email, Long lessonTypeId);

    // ---- Слоти ----
    /** Згенерувати (попередньо переглянути) слоти з проміжку — ще не зберігаючи у БД. */
    List<TimeSlotDTO> previewSlots(String email, GenerateSlotsRequestDTO dto);

    /** Зберегти набір слотів (інтерв'юер уже видалив зайві на фронті). */
    List<TimeSlotDTO> saveSlots(String email, List<TimeSlotDTO> slots);

    List<TimeSlotDTO> getMySlots(String email);

    void deleteSlot(String email, Long slotId);
}
