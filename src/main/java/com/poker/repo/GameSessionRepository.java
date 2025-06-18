package com.poker.repo;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poker.model.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    Optional<GameSession> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
    Optional<GameSession> findTopByOrderByIdDesc();
}
