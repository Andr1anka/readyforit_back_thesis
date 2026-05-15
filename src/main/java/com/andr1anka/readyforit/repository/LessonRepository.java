package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository  extends JpaRepository<Lesson, Long> {
}
