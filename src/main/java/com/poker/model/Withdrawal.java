package com.poker.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private double amount;
    private String addedBy;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;

    public Withdrawal() {
        this.timestamp = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    
}
