package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Review;
import com.andr1anka.readyforit.model.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Відгуки про інтерв'юера (студенти -> інтерв'юер), з пагінацією. Сортування — через Pageable. */
    @Query("""
            SELECT r FROM Review r
            WHERE r.lesson.interviewer.id = :interviewerId
              AND r.reviewType = :type
            """)
    Page<Review> findInterviewerReviews(@Param("interviewerId") Long interviewerId,
                                        @Param("type") ReviewType type,
                                        Pageable pageable);

    /** Середній рейтинг інтерв'юера (відгуки студентів). */
    @Query("""
            SELECT AVG(r.rating) FROM Review r
            WHERE r.lesson.interviewer.id = :interviewerId
              AND r.reviewType = :type
            """)
    Double averageInterviewerRating(@Param("interviewerId") Long interviewerId,
                                    @Param("type") ReviewType type);

    @Query("""
            SELECT COUNT(r) FROM Review r
            WHERE r.lesson.interviewer.id = :interviewerId
              AND r.reviewType = :type
            """)
    long countInterviewerReviews(@Param("interviewerId") Long interviewerId,
                                 @Param("type") ReviewType type);

    /** Відгук, який конкретний користувач залишив до конкретного уроку (якщо є). */
    java.util.Optional<Review> findByLessonIdAndReviewerId(Long lessonId, Long reviewerId);

    /** Відгуки, написані користувачем. */
    java.util.List<Review> findAllByReviewerIdOrderByCreatedAtDesc(Long reviewerId);

    /** Відгуки, отримані користувачем (він — учасник уроку, але не автор). */
    @Query("""
            SELECT r FROM Review r
            LEFT JOIN r.lesson l
            LEFT JOIN l.user su
            LEFT JOIN l.interviewer iv
            LEFT JOIN iv.user iu
            WHERE (su.id = :userId OR iu.id = :userId)
              AND r.reviewer.id <> :userId
            ORDER BY r.createdAt DESC
            """)
    java.util.List<Review> findReceivedByUser(@Param("userId") Long userId);
}
