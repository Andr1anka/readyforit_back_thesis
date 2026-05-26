package com.andr1anka.readyforit.scheduler;

import com.andr1anka.readyforit.model.Lesson;
import com.andr1anka.readyforit.model.LessonStatus;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.LessonRepository;
import com.andr1anka.readyforit.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Надсилає нагадування про заняття:
 *  • за добу (≈24 год) до початку — reminderDaySent;
 *  • за 15 хв до початку — reminder15MinSent.
 *
 * Працює раз на хвилину. Вікна трохи ширші за хвилину, щоб тік нічого не пропускав;
 * прапорці гарантують, що кожне нагадування надсилається рівно один раз.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LessonReminderScheduler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final LessonRepository lessonRepository;
    private final EmailService emailService;

    // кожну хвилину
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();

        // --- за добу: уроки, що почнуться приблизно через 24 години ---
        // вікно [now+24h, now+24h+2хв], ще не нагадані за добу
        LocalDateTime dayFrom = now.plusHours(24);
        LocalDateTime dayTo = now.plusHours(24).plusMinutes(2);
        for (Lesson l : lessonRepository.findBookedBetween(LessonStatus.BOOKED, dayFrom, dayTo)) {
            if (!l.isReminderDaySent()) {
                sendToBoth(l, "Нагадування: заняття завтра",
                        "за добу");
                l.setReminderDaySent(true);
                lessonRepository.save(l);
            }
        }

        // --- за 15 хв: уроки, що почнуться приблизно через 15 хв ---
        LocalDateTime soonFrom = now.plusMinutes(15);
        LocalDateTime soonTo = now.plusMinutes(16);
        for (Lesson l : lessonRepository.findBookedBetween(LessonStatus.BOOKED, soonFrom, soonTo)) {
            if (!l.isReminder15MinSent()) {
                sendToBoth(l, "Нагадування: заняття за 15 хвилин",
                        "за 15 хвилин");
                l.setReminder15MinSent(true);
                lessonRepository.save(l);
            }
        }
    }

    private void sendToBoth(Lesson l, String subject, String whenPhrase) {
        String title = l.getLessonType() == null ? "Заняття" : l.getLessonType().getTitle();
        String when = l.getTimeOfLesson() == null ? "" : l.getTimeOfLesson().format(FMT);
        String joinHint = l.getLink() == null ? "" :
                "\nПриєднатись можна у вкладці «Розклад» застосунку.";

        // студент
        User student = l.getUser();
        if (student != null && student.getEmail() != null) {
            String body = "Вітаємо, " + safe(student.getFirstName()) + "!\n\n"
                    + "Нагадуємо, що ваше заняття «" + title + "» розпочнеться " + whenPhrase
                    + " — " + when + "." + joinHint + "\n\nReadyForIt";
            emailService.sendSimple(student.getEmail(), subject, body);
        }

        // інтерв'юер
        if (l.getInterviewer() != null && l.getInterviewer().getUser() != null) {
            User iu = l.getInterviewer().getUser();
            if (iu.getEmail() != null) {
                String body = "Вітаємо, " + safe(iu.getFirstName()) + "!\n\n"
                        + "Нагадуємо, що ваше заняття «" + title + "» розпочнеться " + whenPhrase
                        + " — " + when + "." + joinHint + "\n\nReadyForIt";
                emailService.sendSimple(iu.getEmail(), subject, body);
            }
        }

        log.info("Reminders ({}) queued for lesson #{}", whenPhrase, l.getId());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
