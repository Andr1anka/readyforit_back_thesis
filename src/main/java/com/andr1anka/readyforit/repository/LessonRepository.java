package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Lesson;
import com.andr1anka.readyforit.model.LessonStatus;
import com.andr1anka.readyforit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByUser(User user);

    List<Lesson> findAllByUserAndStatus(User user, LessonStatus status);

    /** Чи існує урок на цьому слоті (захист від подвійного бронювання). */
    Optional<Lesson> findByTimeId(Long timeSlotId);

    /** Усі уроки, де користувач бере участь — як студент АБО як інтерв'юер. */
    @Query("""
            SELECT l FROM Lesson l
            LEFT JOIN l.user su
            LEFT JOIN l.interviewer iv
            LEFT JOIN iv.user iu
            WHERE su.id = :userId OR iu.id = :userId
            ORDER BY l.timeOfLesson ASC
            """)
    List<Lesson> findAllForParticipant(@Param("userId") Long userId);

    /** Активні уроки, час початку яких у заданому вікні [from, to]. Для нагадувань. */
    @Query("""
            SELECT l FROM Lesson l
            WHERE l.status = :status
              AND l.timeOfLesson BETWEEN :from AND :to
            """)
    List<Lesson> findBookedBetween(@Param("status") LessonStatus status,
                                   @Param("from") java.time.LocalDateTime from,
                                   @Param("to") java.time.LocalDateTime to);
}
