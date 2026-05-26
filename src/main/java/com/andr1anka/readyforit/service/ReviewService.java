package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.*;

import java.util.List;

/**
 * Відгуки, фідбек інтерв'юера та скарги.
 *
 * Важливо:
 * - Review — це публічний відгук/оцінка про іншу сторону уроку. Його може залишити і студент, і інтерв'юер.
 * - Interviewer feedback — це приватна рецензія інтерв'юера по заняттю для студента: що покращити,
 *   де були помилки тощо. Це НЕ впливає на рейтинг і НЕ показується як звичайний відгук.
 */
public interface ReviewService {

    /** Залишити публічний відгук про іншу сторону уроку. */
    ReviewViewDTO submitReview(String email, ReviewRequestDTO request);

    /** Залишити приватний фідбек інтерв'юера по заняттю. */
    ScheduleItemDTO submitInterviewerFeedback(String email, InterviewerFeedbackRequestDTO request);

    /** Відгуки, написані мною. */
    List<ReviewViewDTO> getMyWritten(String email);

    /** Відгуки, отримані про мене. */
    List<ReviewViewDTO> getReceived(String email);

    /** Подати скаргу на іншу сторону уроку. */
    ComplaintViewDTO submitComplaint(String email, ComplaintRequestDTO request);

    /** Мої подані скарги. */
    List<ComplaintViewDTO> getMyComplaints(String email);
}
