package com.poker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

import java.util.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(nullable = true)
    private Double finalAmount;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnoreProperties("player")
    private List<Withdrawal> withdrawals = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "consumers")
    private List<FoodExpense> consumedFood = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "payer")
    private List<FoodExpense> paidFood = new ArrayList<>();
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getFinalAmount() {
        return finalAmount != null ? finalAmount : 0.0;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public List<Withdrawal> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<Withdrawal> withdrawals) {
        this.withdrawals = withdrawals;
    }

    @Transient
    public double getTotalWithdrawals() {
        return withdrawals != null
            ? withdrawals.stream().mapToDouble(Withdrawal::getAmount).sum()
            : 0.0;
    }
}
