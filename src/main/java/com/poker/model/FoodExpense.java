package com.poker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class FoodExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private double amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_id")
    private Player payer;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "food_consumers",
        
        joinColumns = @JoinColumn(name = "food_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> consumers = new ArrayList<>();

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }

    public Player getPayer() { return payer; }

    public void setPayer(Player payer) { this.payer = payer; }

    public List<Player> getConsumers() { return consumers; }

    public void setConsumers(List<Player> consumers) { this.consumers = consumers; }
}
