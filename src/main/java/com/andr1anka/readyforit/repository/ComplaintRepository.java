package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Complaint> findAllByStatusOrderByCreatedAtDesc(com.andr1anka.readyforit.model.ComplaintStatus status);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
