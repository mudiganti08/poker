package com.poker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poker.model.GameSession;
import com.poker.model.Player;
import com.poker.repo.GameSessionRepository;

@Service
public class SessionService {

    @Autowired
    private GameSessionRepository sessionRepo;

    @Autowired
    private PlayerService playerService;

    public GameSession startSession(LocalDate date) {
      
        sessionRepo.findByDate(date).ifPresent(existingSession -> sessionRepo.delete(existingSession));


        GameSession session = new GameSession();
        session.setDate(date);
        sessionRepo.save(session);

        //addDefaultPlayers(session); // ✅ fixed
        return session;
    }

//    private void addDefaultPlayers(GameSession session) {
//        List<Player> defaultPlayers = List.of(
//            new Player("Sridhar", "603-921-3404"),
//            new Player("Satish", "603-888-1234"),
//            new Player("Amar", "603-921-3404"),
//            new Player("Vijay", "603-888-1234"),
//            new Player("Sunil", "603-921-3404"),
//            new Player("Mani", "603-888-1234"),
//            new Player("Rajesh", "603-921-3404"),
//            new Player("Prasad", "978-435-1622"),
//            new Player("Sai", "603-921-3404"),
//            new Player("Pradeep", "603-888-1234"),
//            new Player("Govindh", "603-888-1234")
//        );
//
//        for (Player p : defaultPlayers) {
//            playerService.addPlayer(p, session); // ✅ safe
//        }
//    }

    public boolean sessionExists() {
        return sessionRepo.count() > 0;
    }

    public void deleteSession(GameSession session) {
         sessionRepo.delete(session);
    }
    public Optional<GameSession> getLatestSessionOptional() {
        return sessionRepo.findTopByOrderByIdDesc();
    }
}
