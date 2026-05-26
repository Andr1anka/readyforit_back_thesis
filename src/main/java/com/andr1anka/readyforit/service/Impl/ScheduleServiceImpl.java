package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.LessonJoinInfoDTO;
import com.andr1anka.readyforit.dto.ScheduleItemDTO;
import com.andr1anka.readyforit.dto.ScheduleResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.LessonRepository;
import com.andr1anka.readyforit.repository.ReviewRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private static final int CALENDAR_THRESHOLD = 5;

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getActual(String email) {
        User user = getUser(email);
        LocalDateTime now = LocalDateTime.now();

        List<ScheduleItemDTO> items = lessonRepository.findAllForParticipant(user.getId()).stream()
                .filter(l -> isActual(l, now))
                .sorted(Comparator.comparing(l -> l.getTimeOfLesson() == null ? LocalDateTime.MAX : l.getTimeOfLesson()))
                .map(l -> toItem(l, user))
                .toList();

        return ScheduleResponseDTO.builder()
                .items(new ArrayList<>(items))
                .actualCount(items.size())
                .suggestedView(items.size() >= CALENDAR_THRESHOLD ? "calendar" : "list")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getArchived(String email) {
        User user = getUser(email);
        LocalDateTime now = LocalDateTime.now();

        List<ScheduleItemDTO> items = lessonRepository.findAllForParticipant(user.getId()).stream()
                .filter(l -> !isActual(l, now))
                .sorted(Comparator.comparing(
                        (Lesson l) -> l.getTimeOfLesson() == null ? LocalDateTime.MIN : l.getTimeOfLesson())
                        .reversed())
                .map(l -> toItem(l, user))
                .toList();

        return ScheduleResponseDTO.builder()
                .items(new ArrayList<>(items))
                .actualCount(items.size())
                .suggestedView("list")
                .build();
    }

    // актуальний = активний (BOOKED) і час ще не минув
    private boolean isActual(Lesson l, LocalDateTime now) {
        if (l.getStatus() == LessonStatus.CANCELLED) return false;
        if (l.getStatus() == LessonStatus.COMPLETED) return false;
        LocalDateTime t = l.getTimeOfLesson();
        return t != null && t.isAfter(now);
    }

    private ScheduleItemDTO toItem(Lesson l, User currentUser) {
        boolean iAmInterviewer = l.getInterviewer() != null
                && l.getInterviewer().getUser() != null
                && l.getInterviewer().getUser().getId().equals(currentUser.getId());

        User counterpart = iAmInterviewer ? l.getUser()
                : (l.getInterviewer() == null ? null : l.getInterviewer().getUser());

        TimeSlots slot = l.getTime();
        boolean isArchived = !isActual(l, LocalDateTime.now());

        // відгук, який залишив поточний користувач
        Integer myRating = null;
        String myComment = null;
        var existing = reviewRepository.findByLessonIdAndReviewerId(l.getId(), currentUser.getId());
        if (existing.isPresent()) {
            myRating = existing.get().getRating();
            myComment = existing.get().getComment();
        }

        // можна залишити відгук, якщо урок в архіві (минув/завершений), не скасований і ще не залишено
        boolean canReview = isArchived
                && l.getStatus() != LessonStatus.CANCELLED
                && existing.isEmpty();

        return ScheduleItemDTO.builder()
                .lessonId(l.getId())
                .title(l.getLessonType() == null ? "Заняття" : l.getLessonType().getTitle())
                .counterpartFirstName(counterpart == null ? null : counterpart.getFirstName())
                .counterpartLastName(counterpart == null ? null : counterpart.getLastName())
                .counterpartPhoto(counterpart == null ? null : counterpart.getPicture())
                .role(iAmInterviewer ? "INTERVIEWER" : "STUDENT")
                .date(slot == null ? null : slot.getDate())
                .startTime(slot == null ? null : slot.getStartTime())
                .endTime(slot == null ? null : slot.getEndTime())
                .timeOfLesson(l.getTimeOfLesson())
                .durationMinutes(l.getDurationMinutes())
                .price(l.getPrice())
                .status(l.getStatus())
                .link(l.getLink())
                .reviewFromInterviewer(l.getReviewFromInterviewer())
                .myRating(myRating)
                .myReviewComment(myComment)
                .canReview(canReview)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
    }

    @Override
    @Transactional(readOnly = true)
    public LessonJoinInfoDTO getJoinInfo(String email, Long lessonId) {
        User user = getUser(email);
        Lesson l = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BadRequestException("Урок не знайдено"));

        boolean iAmInterviewer = l.getInterviewer() != null
                && l.getInterviewer().getUser() != null
                && l.getInterviewer().getUser().getId().equals(user.getId());
        boolean iAmStudent = l.getUser() != null && l.getUser().getId().equals(user.getId());
        if (!iAmInterviewer && !iAmStudent) {
            throw new BadRequestException("Ви не є учасником цього заняття");
        }

        User counterpart = iAmInterviewer ? l.getUser()
                : (l.getInterviewer() == null ? null : l.getInterviewer().getUser());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = l.getTimeOfLesson();
        boolean joinable = false;
        if (start != null) {
            LocalDateTime open = start.minusMinutes(15);
            LocalDateTime end = start.plusMinutes(l.getDurationMinutes() == null ? 60 : l.getDurationMinutes());
            joinable = !now.isBefore(open) && !now.isAfter(end);
        }

        return LessonJoinInfoDTO.builder()
                .lessonId(l.getId())
                .title(l.getLessonType() == null ? "Заняття" : l.getLessonType().getTitle())
                .room(l.getLink())
                .counterpartFirstName(counterpart == null ? null : counterpart.getFirstName())
                .counterpartLastName(counterpart == null ? null : counterpart.getLastName())
                .timeOfLesson(start)
                .joinable(joinable)
                .build();
    }
}
