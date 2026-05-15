package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Attachment;
import com.andr1anka.readyforit.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
