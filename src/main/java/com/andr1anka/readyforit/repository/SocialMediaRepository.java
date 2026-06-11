package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Interviewer;
import com.andr1anka.readyforit.model.SocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialMediaRepository extends JpaRepository<SocialMedia, Long> {
    List<SocialMedia> findAllByInterviewerOrderByIdAsc(Interviewer interviewer);
}

