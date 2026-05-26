package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Interviewer;
import com.andr1anka.readyforit.model.TimeSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotsRepository extends JpaRepository<TimeSlots,Long> {
    List<TimeSlots> findAllByInterviewerOrderByDateAscStartTimeAsc(Interviewer interviewer);
    List<TimeSlots> findAllByInterviewerIdOrderByDateAscStartTimeAsc(Long interviewerId);
    List<TimeSlots> findAllByInterviewerAndDate(Interviewer interviewer, LocalDate date);
}
