package com.poker.dto;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class SessionRequest {
    private LocalDate date;
    private List<UUID> players; // Not used for now, but good to have for future

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void setPlayers(List<UUID> players) {
        this.players = players;
    }
}

