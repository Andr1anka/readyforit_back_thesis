package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.*;

import java.util.List;

/**
 * Відгуки та скарги (Feature 6). Відгук/скаргу можна подати лише за наявності
 * спільного уроку. Рецензія інтерв'юера завершує урок і перераховує кошти.
 */
public interface ReviewService {

    /** Залишити відгук про іншу сторону уроку. */
    ReviewViewDTO submitReview(String email, ReviewRequestDTO request);

    /** Відгуки, написані мною. */
    List<ReviewViewDTO> getMyWritten(String email);

    /** Відгуки, отримані про мене. */
    List<ReviewViewDTO> getReceived(String email);

    /** Подати скаргу на іншу сторону уроку. */
    ComplaintViewDTO submitComplaint(String email, ComplaintRequestDTO request);

    /** Мої подані скарги. */
    List<ComplaintViewDTO> getMyComplaints(String email);
}
