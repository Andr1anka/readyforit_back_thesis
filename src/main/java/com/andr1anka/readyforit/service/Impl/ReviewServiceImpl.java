package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.*;
import com.andr1anka.readyforit.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ReviewRepository reviewRepository;
    private final ComplaintRepository complaintRepository;

    // ---------------------------------------------------------------- ВІДГУКИ
    @Override
    @Transactional
    public ReviewViewDTO submitReview(String email, ReviewRequestDTO request) {
        User me = getUser(email);
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new BadRequestException("Урок не знайдено"));

        Role roleInLesson = participantRole(lesson, me);   // кидає, якщо не учасник

        // відгук лише після того, як урок відбувся (час минув) і не скасований
        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            throw new BadRequestException("Урок скасовано — відгук недоступний");
        }
        if (lesson.getTimeOfLesson() == null || lesson.getTimeOfLesson().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Відгук можна залишити лише після проведення заняття");
        }

        // один відгук на урок від користувача
        if (reviewRepository.findByLessonIdAndReviewerId(lesson.getId(), me.getId()).isPresent()) {
            throw new BadRequestException("Ви вже залишили відгук до цього заняття");
        }

        boolean iAmInterviewer = roleInLesson == Role.INTERVIEWER;
        ReviewType type = iAmInterviewer
                ? ReviewType.INTERVIEWER_TO_STUDENT
                : ReviewType.STUDENT_TO_INTERVIEWER;

        Review review = Review.builder()
                .lesson(lesson)
                .reviewer(me)
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewType(type)
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        // якщо це рецензія ІНТЕРВ'ЮЕРА — урок вважається проведеним:
        // перераховуємо кошти інтерв'юеру (escrow -> баланс) і завершуємо урок
        if (iAmInterviewer) {
            lesson.setReviewFromInterviewer(request.getComment());
            payoutInterviewer(lesson);
        }

        // оновлюємо середній рейтинг адресата
        recalcRank(counterpartUser(lesson, me));

        return toReviewView(review, me);
    }

    private void payoutInterviewer(Lesson lesson) {
        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            return; // вже виплачено
        }
        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            return;
        }
        User interviewerUser = lesson.getInterviewer() == null ? null : lesson.getInterviewer().getUser();
        if (interviewerUser != null && lesson.getPrice() != null) {
            BigDecimal bal = interviewerUser.getBalance() == null ? BigDecimal.ZERO : interviewerUser.getBalance();
            interviewerUser.setBalance(bal.add(BigDecimal.valueOf(lesson.getPrice())));
            userRepository.save(interviewerUser);
        }
        lesson.setStatus(LessonStatus.COMPLETED);
        lessonRepository.save(lesson);
        log.info("Lesson #{} completed, interviewer paid {}", lesson.getId(), lesson.getPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewViewDTO> getMyWritten(String email) {
        User me = getUser(email);
        return reviewRepository.findAllByReviewerIdOrderByCreatedAtDesc(me.getId())
                .stream().map(r -> toReviewView(r, me)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewViewDTO> getReceived(String email) {
        User me = getUser(email);
        return reviewRepository.findReceivedByUser(me.getId())
                .stream().map(r -> toReviewView(r, me)).toList();
    }

    // ---------------------------------------------------------------- СКАРГИ
    @Override
    @Transactional
    public ComplaintViewDTO submitComplaint(String email, ComplaintRequestDTO request) {
        User me = getUser(email);
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new BadRequestException("Урок не знайдено"));

        participantRole(lesson, me); // перевірка участі
        User accused = counterpartUser(lesson, me);
        if (accused == null) {
            throw new BadRequestException("Неможливо визначити іншу сторону уроку");
        }

        Complaint complaint = new Complaint();
        complaint.setUser(me);
        complaint.setAccusedUser(accused);
        complaint.setLesson(lesson);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setStatus(ComplaintStatus.OPEN);
        complaintRepository.save(complaint);

        return toComplaintView(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintViewDTO> getMyComplaints(String email) {
        User me = getUser(email);
        return complaintRepository.findAllByUserIdOrderByCreatedAtDesc(me.getId())
                .stream().map(this::toComplaintView).toList();
    }

    // ---------------------------------------------------------------- ДОПОМІЖНІ
    private Role participantRole(Lesson lesson, User me) {
        boolean iAmInterviewer = lesson.getInterviewer() != null
                && lesson.getInterviewer().getUser() != null
                && lesson.getInterviewer().getUser().getId().equals(me.getId());
        boolean iAmStudent = lesson.getUser() != null && lesson.getUser().getId().equals(me.getId());
        if (iAmInterviewer) return Role.INTERVIEWER;
        if (iAmStudent) return Role.USER;
        throw new BadRequestException("Ви не були учасником цього заняття");
    }

    private User counterpartUser(Lesson lesson, User me) {
        boolean iAmInterviewer = lesson.getInterviewer() != null
                && lesson.getInterviewer().getUser() != null
                && lesson.getInterviewer().getUser().getId().equals(me.getId());
        return iAmInterviewer ? lesson.getUser()
                : (lesson.getInterviewer() == null ? null : lesson.getInterviewer().getUser());
    }

    private void recalcRank(User target) {
        if (target == null) return;
        // середній рейтинг по всіх отриманих відгуках
        List<Review> received = reviewRepository.findReceivedByUser(target.getId());
        if (received.isEmpty()) return;
        double avg = received.stream().mapToInt(Review::getRating).average().orElse(0);
        target.setRank(Math.round(avg * 10.0) / 10.0);
        userRepository.save(target);
    }

    private ReviewViewDTO toReviewView(Review r, User me) {
        Lesson lesson = r.getLesson();
        // інша сторона відносно "me"
        User counterpart;
        if (r.getReviewer() != null && r.getReviewer().getId().equals(me.getId())) {
            counterpart = counterpartUser(lesson, me);          // я автор -> про кого
        } else {
            counterpart = r.getReviewer();                       // отриманий -> від кого
        }
        return ReviewViewDTO.builder()
                .id(r.getId())
                .lessonId(lesson == null ? null : lesson.getId())
                .lessonTitle(lesson == null || lesson.getLessonType() == null ? "Заняття" : lesson.getLessonType().getTitle())
                .counterpartFirstName(counterpart == null ? null : counterpart.getFirstName())
                .counterpartLastName(counterpart == null ? null : counterpart.getLastName())
                .counterpartPhoto(counterpart == null ? null : counterpart.getPicture())
                .rating(r.getRating())
                .comment(r.getComment())
                .reviewType(r.getReviewType())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private ComplaintViewDTO toComplaintView(Complaint c) {
        return ComplaintViewDTO.builder()
                .id(c.getId())
                .lessonId(c.getLesson() == null ? null : c.getLesson().getId())
                .lessonTitle(c.getLesson() == null || c.getLesson().getLessonType() == null
                        ? "Заняття" : c.getLesson().getLessonType().getTitle())
                .accusedFirstName(c.getAccusedUser() == null ? null : c.getAccusedUser().getFirstName())
                .accusedLastName(c.getAccusedUser() == null ? null : c.getAccusedUser().getLastName())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .adminComment(c.getAdminComment())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
    }
}
