package com.poker.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poker.model.GameSession;
import com.poker.model.Player;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<GameSession> findByNameAndSessionId(String name, UUID sessionId);
    List<Player> findAllBySessionId(UUID sessionId); // FIXED Long -> UUID
    void deleteById(UUID playerId); // FIXED Long -> UUID
    List<Player> findBySession(GameSession session);
    

    @Query("SELECT p FROM Player p WHERE p.session.id = :sessionId")
    List<Player> findPlayersBySessionId(@Param("sessionId") UUID sessionId);

}
