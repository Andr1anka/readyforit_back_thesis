package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.TimeSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotsRepository extends JpaRepository<TimeSlots,Long> {
}
