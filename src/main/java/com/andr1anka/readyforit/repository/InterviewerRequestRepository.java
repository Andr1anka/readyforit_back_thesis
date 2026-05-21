package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.InterviewerRequest;
import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import com.andr1anka.readyforit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewerRequestRepository extends JpaRepository<InterviewerRequest, Long> {
    Optional<InterviewerRequest> findTopByUserOrderByCreatedAtDesc(User user);
    List<InterviewerRequest> findAllByStatus(InterviewerRequestStatus status);
}