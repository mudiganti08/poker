package com.poker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private double finalAmount;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Withdrawal> withdrawals = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public List<Withdrawal> getWithdrawals() { return withdrawals; }
    public void setWithdrawals(List<Withdrawal> withdrawals) { this.withdrawals = withdrawals; }

    @Transient
    public double getTotalWithdrawals() {
        return withdrawals.stream().mapToDouble(Withdrawal::getAmount).sum();
    }
}
