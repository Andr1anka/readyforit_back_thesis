package com.andr1anka.readyforit.repository;

import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByVerificationStatus(VerificationStatus status);
    List<User> findAllByOrderByIdAsc();
}
