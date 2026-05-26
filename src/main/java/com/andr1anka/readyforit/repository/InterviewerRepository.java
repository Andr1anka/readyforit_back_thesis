package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Interviewer;
import com.andr1anka.readyforit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
    Optional<Interviewer> findByUser(User user);
    Optional<Interviewer> findByUserId(Long userId);
}
