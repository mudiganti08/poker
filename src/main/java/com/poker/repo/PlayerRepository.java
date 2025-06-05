package com.poker.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poker.model.Player;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
}
