package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.InformationAboutLessonRepository;
import com.andr1anka.readyforit.repository.InterviewerRepository;
import com.andr1anka.readyforit.repository.TimeSlotsRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.InterviewerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewerProfileServiceImpl implements InterviewerProfileService {

    private static final int DEFAULT_DURATION = 60;
    private static final int DEFAULT_BREAK = 10;

    private final UserRepository userRepository;
    private final InterviewerRepository interviewerRepository;
    private final InformationAboutLessonRepository lessonTypeRepository;
    private final TimeSlotsRepository timeSlotsRepository;

    // ---------------------------------------------------------------------
    // Налаштування
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public InterviewerSettingsDTO getMySettings(String email) {
        Interviewer interviewer = getOrCreateInterviewer(email);
        return InterviewerSettingsDTO.builder()
                .plannedSessionDurationMinutes(interviewer.getPlannedSessionDurationMinutes())
                .expectedTimeForBreak(interviewer.getExpectedTimeForBreak() == null
                        ? DEFAULT_BREAK : interviewer.getExpectedTimeForBreak())
                .build();
    }

    @Override
    @Transactional
    public InterviewerSettingsDTO updateMySettings(String email, InterviewerSettingsDTO dto) {
        Interviewer interviewer = getOrCreateInterviewer(email);
        interviewer.setPlannedSessionDurationMinutes(dto.getPlannedSessionDurationMinutes());
        interviewer.setExpectedTimeForBreak(dto.getExpectedTimeForBreak());
        interviewerRepository.save(interviewer);
        return dto;
    }

    // ---------------------------------------------------------------------
    // Види занять
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<LessonTypeResponseDTO> getMyLessonTypes(String email) {
        Interviewer interviewer = getInterviewerOrThrow(email);
        return lessonTypeRepository.findAllByInterviewer(interviewer).stream()
                .map(lt -> toLessonTypeDto(lt, interviewer))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonTypeResponseDTO createLessonType(String email, LessonTypeRequestDTO dto) {
        Interviewer interviewer = getOrCreateInterviewer(email);

        InformationAboutLesson lt = InformationAboutLesson.builder()
                .title(dto.getTitle().trim())
                .shortDescription(dto.getShortDescription().trim())
                .longDescription(dto.getLongDescription())
                .specializations(joinTags(dto.getTags()))
                .price(dto.getPrice())
                .interviewer(interviewer)
                .build();
        lessonTypeRepository.save(lt);
        return toLessonTypeDto(lt, interviewer);
    }

    @Override
    @Transactional
    public LessonTypeResponseDTO updateLessonType(String email, Long lessonTypeId, LessonTypeRequestDTO dto) {
        Interviewer interviewer = getInterviewerOrThrow(email);
        InformationAboutLesson lt = lessonTypeRepository.findById(lessonTypeId)
                .orElseThrow(() -> new BadRequestException("Вид заняття не знайдено"));
        if (!lt.getInterviewer().getId().equals(interviewer.getId())) {
            throw new BadRequestException("Це заняття належить іншому інтерв'юеру");
        }
        lt.setTitle(dto.getTitle().trim());
        lt.setShortDescription(dto.getShortDescription().trim());
        lt.setLongDescription(dto.getLongDescription());
        lt.setSpecializations(joinTags(dto.getTags()));
        lt.setPrice(dto.getPrice());
        lessonTypeRepository.save(lt);
        return toLessonTypeDto(lt, interviewer);
    }

    @Override
    @Transactional
    public void deleteLessonType(String email, Long lessonTypeId) {
        Interviewer interviewer = getInterviewerOrThrow(email);
        InformationAboutLesson lt = lessonTypeRepository.findById(lessonTypeId)
                .orElseThrow(() -> new BadRequestException("Вид заняття не знайдено"));
        if (!lt.getInterviewer().getId().equals(interviewer.getId())) {
            throw new BadRequestException("Це заняття належить іншому інтерв'юеру");
        }
        lessonTypeRepository.delete(lt);
    }

    // ---------------------------------------------------------------------
    // Слоти
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public List<TimeSlotDTO> previewSlots(String email, GenerateSlotsRequestDTO dto) {
        Interviewer interviewer = getOrCreateInterviewer(email);

        if (dto.getFrom() == null || dto.getTo() == null || !dto.getTo().isAfter(dto.getFrom())) {
            throw new BadRequestException("Час 'до' має бути пізніше за 'з'");
        }
        if (dto.getDate() == null || dto.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Дата має бути сьогодні або в майбутньому");
        }

        int baseDuration = interviewer.getPlannedSessionDurationMinutes() == null
                ? DEFAULT_DURATION : interviewer.getPlannedSessionDurationMinutes();
        int breakMinutes = interviewer.getExpectedTimeForBreak() == null
                ? DEFAULT_BREAK : interviewer.getExpectedTimeForBreak();

        // Слоти нарізаються за базовою тривалістю інтерв'юера.
        // Ця сама тривалість застосовується до всіх видів занять цього інтерв'юера.
        int slotDuration = baseDuration;
        if (slotDuration < 5) slotDuration = 5;

        List<TimeSlotDTO> result = new ArrayList<>();
        LocalTime cursor = dto.getFrom();
        // нарізаємо проміжок: [cursor, cursor+slotDuration], потім перерва
        while (!cursor.plusMinutes(slotDuration).isAfter(dto.getTo())) {
            LocalTime end = cursor.plusMinutes(slotDuration);
            result.add(TimeSlotDTO.builder()
                    .date(dto.getDate())
                    .startTime(cursor)
                    .endTime(end)
                    .available(true)
                    .booked(false)
                    .durationMinutes(slotDuration)
                    .build());
            cursor = end.plusMinutes(breakMinutes);
        }

        if (result.isEmpty()) {
            throw new BadRequestException(
                    "Проміжок замалий для жодного слота тривалістю " + slotDuration + " хв");
        }
        return result;
    }

    @Override
    @Transactional
    public List<TimeSlotDTO> saveSlots(String email, List<TimeSlotDTO> slots) {
        Interviewer interviewer = getOrCreateInterviewer(email);
        if (slots == null || slots.isEmpty()) {
            throw new BadRequestException("Немає слотів для збереження");
        }

        // вже наявні слоти інтерв'юера — щоб не дублювати/не перетинати
        List<TimeSlots> existing = timeSlotsRepository
                .findAllByInterviewerOrderByDateAscStartTimeAsc(interviewer);

        List<TimeSlots> toSave = new ArrayList<>();
        for (TimeSlotDTO s : slots) {
            if (s.getDate() == null || s.getStartTime() == null || s.getEndTime() == null) {
                throw new BadRequestException("Некоректний слот");
            }
            if (!s.getEndTime().isAfter(s.getStartTime())) {
                throw new BadRequestException("Кінець слота має бути пізніше за початок");
            }
            if (overlapsAny(s, existing) || overlapsAny(s, toSave)) {
                // мовчки пропускаємо перетини, щоб збереження не падало через 1 дублікат
                log.info("Skipping overlapping slot {} {}-{}", s.getDate(), s.getStartTime(), s.getEndTime());
                continue;
            }
            TimeSlots ts = new TimeSlots();
            ts.setDate(s.getDate());
            ts.setStartTime(s.getStartTime());
            ts.setEndTime(s.getEndTime());
            ts.setAvailable(true);
            ts.setInterviewer(interviewer);
            toSave.add(ts);
        }

        if (toSave.isEmpty()) {
            throw new BadRequestException("Усі слоти перетинаються з наявними");
        }

        List<TimeSlots> saved = timeSlotsRepository.saveAll(toSave);
        return saved.stream().map(this::toSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getMySlots(String email) {
        Interviewer interviewer = getInterviewerOrThrow(email);
        return timeSlotsRepository.findAllByInterviewerOrderByDateAscStartTimeAsc(interviewer)
                .stream().map(this::toSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSlot(String email, Long slotId) {
        Interviewer interviewer = getInterviewerOrThrow(email);
        TimeSlots slot = timeSlotsRepository.findById(slotId)
                .orElseThrow(() -> new BadRequestException("Слот не знайдено"));
        if (!slot.getInterviewer().getId().equals(interviewer.getId())) {
            throw new BadRequestException("Слот належить іншому інтерв'юеру");
        }
        if (slot.getLesson() != null) {
            throw new BadRequestException("Не можна видалити слот, на який вже є запис");
        }
        timeSlotsRepository.delete(slot);
    }

    // ---------------------------------------------------------------------
    // Допоміжні
    // ---------------------------------------------------------------------
    private boolean overlapsAny(TimeSlotDTO s, List<TimeSlots> list) {
        for (TimeSlots ts : list) {
            if (ts.getDate().equals(s.getDate())
                    && s.getStartTime().isBefore(ts.getEndTime())
                    && ts.getStartTime().isBefore(s.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private Interviewer getOrCreateInterviewer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        if (user.getRole() != Role.INTERVIEWER) {
            throw new BadRequestException("Доступно лише інтерв'юерам");
        }
        return interviewerRepository.findByUser(user).orElseGet(() -> {
            Interviewer i = new Interviewer();
            i.setUser(user);
            i.setPlannedSessionDurationMinutes(DEFAULT_DURATION);
            i.setExpectedTimeForBreak(DEFAULT_BREAK);
            return interviewerRepository.save(i);
        });
    }

    private Interviewer getInterviewerOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        return interviewerRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Профіль інтерв'юера ще не налаштовано"));
    }

    private String joinTags(List<String> tags) {
        return tags.stream().map(String::trim).filter(t -> !t.isEmpty())
                .collect(Collectors.joining(","));
    }

    private List<String> splitTags(String specializations) {
        if (specializations == null || specializations.isBlank()) return new ArrayList<>();
        return Arrays.stream(specializations.split(","))
                .map(String::trim).filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    private LessonTypeResponseDTO toLessonTypeDto(InformationAboutLesson lt, Interviewer interviewer) {
        int base = interviewer.getPlannedSessionDurationMinutes() == null
                ? DEFAULT_DURATION : interviewer.getPlannedSessionDurationMinutes();
        return LessonTypeResponseDTO.builder()
                .id(lt.getId())
                .title(lt.getTitle())
                .shortDescription(lt.getShortDescription())
                .longDescription(lt.getLongDescription())
                .tags(splitTags(lt.getSpecializations()))
                .price(lt.getPrice())
                .durationMultiplier(1.0)
                .effectiveDurationMinutes(base)
                .build();
    }

    private TimeSlotDTO toSlotDto(TimeSlots ts) {
        int duration = (int) java.time.Duration.between(ts.getStartTime(), ts.getEndTime()).toMinutes();
        return TimeSlotDTO.builder()
                .id(ts.getId())
                .date(ts.getDate())
                .startTime(ts.getStartTime())
                .endTime(ts.getEndTime())
                .available(ts.isAvailable() && ts.getLesson() == null)
                .booked(ts.getLesson() != null)
                .durationMinutes(duration)
                .build();
    }
}
