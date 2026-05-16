package com.backend.backend.repository;

import com.backend.backend.model.entity.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DifficultyRepository extends JpaRepository<Difficulty, Integer> {
}
