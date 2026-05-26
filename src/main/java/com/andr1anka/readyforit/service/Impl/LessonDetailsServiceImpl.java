package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.LessonDetailsDTO;
import com.andr1anka.readyforit.dto.PagedResponseDTO;
import com.andr1anka.readyforit.dto.ReviewItemDTO;
import com.andr1anka.readyforit.dto.TimeSlotDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.InformationAboutLessonRepository;
import com.andr1anka.readyforit.repository.InterviewerRequestRepository;
import com.andr1anka.readyforit.repository.ReviewRepository;
import com.andr1anka.readyforit.repository.TimeSlotsRepository;
import com.andr1anka.readyforit.service.LessonDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonDetailsServiceImpl implements LessonDetailsService {

    private static final int DEFAULT_DURATION = 60;

    private final InformationAboutLessonRepository lessonTypeRepository;
    private final TimeSlotsRepository timeSlotsRepository;
    private final ReviewRepository reviewRepository;
    private final InterviewerRequestRepository requestRepository;

    @Override
    @Transactional(readOnly = true)
    public LessonDetailsDTO getLessonDetails(Long lessonTypeId) {
        InformationAboutLesson lt = lessonTypeRepository.findById(lessonTypeId)
                .orElseThrow(() -> new BadRequestException("Вид заняття не знайдено"));

        Interviewer interviewer = lt.getInterviewer();
        if (interviewer == null) {
            throw new BadRequestException("У заняття немає інтерв'юера");
        }
        User user = interviewer.getUser();

        int base = interviewer.getPlannedSessionDurationMinutes() == null
                ? DEFAULT_DURATION : interviewer.getPlannedSessionDurationMinutes();
        int effectiveDuration = base;

        // слоти інтерв'юера (майбутні), фіолетові вільні / сірі зайняті
        List<TimeSlotDTO> slots = timeSlotsRepository
                .findAllByInterviewerOrderByDateAscStartTimeAsc(interviewer)
                .stream()
                .map(this::toSlotDto)
                .collect(Collectors.toList());

        Double avg = reviewRepository.averageInterviewerRating(
                interviewer.getId(), ReviewType.STUDENT_TO_INTERVIEWER);
        long reviewsCount = reviewRepository.countInterviewerReviews(
                interviewer.getId(), ReviewType.STUDENT_TO_INTERVIEWER);

        Integer experienceYears = user == null ? null :
                requestRepository.findTopByUserOrderByCreatedAtDesc(user)
                        .map(InterviewerRequest::getYearsOfExperience)
                        .orElse(null);

        return LessonDetailsDTO.builder()
                .lessonTypeId(lt.getId())
                .interviewerId(interviewer.getId())
                .interviewerFirstName(user == null ? null : user.getFirstName())
                .interviewerLastName(user == null ? null : user.getLastName())
                .interviewerPhoto(user != null && user.getPicture() != null && !user.getPicture().isBlank()
                        ? "/api/user/me/avatar?u=" + user.getId()
                        : null)
                .interviewerRank(avg == null ? (user == null ? null : user.getRank()) : round1(avg))
                .reviewsCount(reviewsCount)
                .verified(user != null && user.isVerificated())
                .experienceYears(experienceYears)
                .title(lt.getTitle())
                .shortDescription(lt.getShortDescription())
                .longDescription(lt.getLongDescription())
                .tags(splitTags(lt.getSpecializations()))
                .price(lt.getPrice())
                .durationMultiplier(1.0)
                .effectiveDurationMinutes(effectiveDuration)
                .slots(slots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ReviewItemDTO> getInterviewerReviews(Long interviewerId, int page, int size, String sort) {
        if (page < 0) page = 0;
        if (size <= 0) size = 5;

        Sort sortSpec = switch (sort == null ? "" : sort) {
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "rating_asc" -> Sort.by(Sort.Direction.ASC, "rating");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // newest
        };
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<Review> reviewPage = reviewRepository.findInterviewerReviews(
                interviewerId, ReviewType.STUDENT_TO_INTERVIEWER, pageable);

        List<ReviewItemDTO> content = reviewPage.getContent().stream()
                .map(this::toReviewDto)
                .collect(Collectors.toList());

        return PagedResponseDTO.<ReviewItemDTO>builder()
                .content(content)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .build();
    }

    // ---- допоміжні ----
    private ReviewItemDTO toReviewDto(Review r) {
        User reviewer = r.getReviewer();
        return ReviewItemDTO.builder()
                .id(r.getId())
                .reviewerFirstName(reviewer == null ? null : reviewer.getFirstName())
                .reviewerLastName(reviewer == null ? null : reviewer.getLastName())
                .reviewerPhoto(reviewer == null ? null : reviewer.getPicture())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private TimeSlotDTO toSlotDto(TimeSlots ts) {
        int duration = (int) Duration.between(ts.getStartTime(), ts.getEndTime()).toMinutes();
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

    private List<String> splitTags(String specializations) {
        if (specializations == null || specializations.isBlank()) return new ArrayList<>();
        return Arrays.stream(specializations.split(","))
                .map(String::trim).filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    private Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
