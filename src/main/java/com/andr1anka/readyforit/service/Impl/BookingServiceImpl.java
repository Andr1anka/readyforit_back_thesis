package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.BookingRequestDTO;
import com.andr1anka.readyforit.dto.BookingResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.InformationAboutLessonRepository;
import com.andr1anka.readyforit.repository.LessonRepository;
import com.andr1anka.readyforit.repository.TimeSlotsRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final TimeSlotsRepository timeSlotsRepository;
    private final InformationAboutLessonRepository lessonTypeRepository;

    @Override
    @Transactional
    public BookingResponseDTO book(String email, BookingRequestDTO request) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (student.isBlocked()) {
            throw new BadRequestException("Ваш акаунт заблоковано");
        }

        InformationAboutLesson lessonType = lessonTypeRepository.findById(request.getLessonTypeId())
                .orElseThrow(() -> new BadRequestException("Вид заняття не знайдено"));

        TimeSlots slot = timeSlotsRepository.findById(request.getSlotId())
                .orElseThrow(() -> new BadRequestException("Слот не знайдено"));

        Interviewer interviewer = slot.getInterviewer();
        if (interviewer == null) {
            throw new BadRequestException("Слот не належить жодному інтерв'юеру");
        }

        // слот має належати тому ж інтерв'юеру, що й вид заняття
        if (lessonType.getInterviewer() == null
                || !lessonType.getInterviewer().getId().equals(interviewer.getId())) {
            throw new BadRequestException("Слот не відповідає обраному заняттю");
        }

        // не можна записатись до самого себе
        if (interviewer.getUser() != null && interviewer.getUser().getId().equals(student.getId())) {
            throw new BadRequestException("Не можна записатись на власне заняття");
        }

        // слот має бути вільний
        if (!slot.isAvailable() || slot.getLesson() != null) {
            throw new BadRequestException("Цей слот вже зайнятий");
        }

        // слот має бути в майбутньому
        LocalDateTime slotStart = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        LocalDateTime slotEnd = LocalDateTime.of(slot.getDate(), slot.getEndTime());
        if (slotStart.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Не можна записатись на слот, що вже минув");
        }

        // перевірка перетинів із наявними уроками студента (тільки активні)
        List<Lesson> studentLessons = lessonRepository.findAllByUserAndStatus(student, LessonStatus.BOOKED);
        for (Lesson l : studentLessons) {
            if (l.getTime() == null) continue;
            LocalDateTime existingStart = LocalDateTime.of(l.getTime().getDate(), l.getTime().getStartTime());
            LocalDateTime existingEnd = LocalDateTime.of(l.getTime().getDate(), l.getTime().getEndTime());
            if (slotStart.isBefore(existingEnd) && existingStart.isBefore(slotEnd)) {
                throw new BadRequestException(
                        "На цей час у вас вже заплановано заняття");
            }
        }

        // достатність балансу
        int price = lessonType.getPrice();
        BigDecimal priceBd = BigDecimal.valueOf(price);
        BigDecimal balance = student.getBalance() == null ? BigDecimal.ZERO : student.getBalance();
        if (balance.compareTo(priceBd) < 0) {
            throw new BadRequestException(
                    "Недостатньо коштів. Потрібно " + price + " грн, на балансі " + balance + " грн");
        }

        // --- усе ок: списуємо кошти (escrow) і створюємо урок ---
        student.setBalance(balance.subtract(priceBd));
        userRepository.save(student);

        int durationMinutes = (int) Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();

        Lesson lesson = new Lesson();
        lesson.setUser(student);
        lesson.setInterviewer(interviewer);
        lesson.setTime(slot);
        lesson.setLessonType(lessonType);
        lesson.setTimeOfLesson(slotStart);
        lesson.setPrice(price);
        lesson.setDurationMinutes(durationMinutes);
        lesson.setStatus(LessonStatus.BOOKED);
        lesson.setLink(generateMeetingLink());

        slot.setAvailable(false);
        slot.setLesson(lesson);
        // Lesson володіє звʼязком (time_slot_id) і має cascade на slot,
        // тож достатньо зберегти lesson — slot оновиться в межах транзакції.
        lessonRepository.save(lesson);

        User interviewerUser = interviewer.getUser();

        return BookingResponseDTO.builder()
                .lessonId(lesson.getId())
                .lessonTitle(lessonType.getTitle())
                .interviewerFirstName(interviewerUser == null ? null : interviewerUser.getFirstName())
                .interviewerLastName(interviewerUser == null ? null : interviewerUser.getLastName())
                .timeOfLesson(slotStart)
                .price(price)
                .durationMinutes(durationMinutes)
                .status(lesson.getStatus())
                .link(lesson.getLink())
                .newBalance(student.getBalance())
                .build();
    }

    private String generateMeetingLink() {
        // унікальна кімната для відеозвʼязку (Feature 5 під'єднається сюди)
        return "rfi-" + UUID.randomUUID().toString().substring(0, 12);
    }
}
