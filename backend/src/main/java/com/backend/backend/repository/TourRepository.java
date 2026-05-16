package com.backend.backend.repository;

import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    List<Tour> findByUser(User user);
    Optional<Tour> findByIdAndUser(int id, User user);
}
