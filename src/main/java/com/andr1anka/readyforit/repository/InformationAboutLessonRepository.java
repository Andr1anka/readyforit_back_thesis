package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.InformationAboutLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformationAboutLessonRepository extends JpaRepository<InformationAboutLesson, Long> {
}
