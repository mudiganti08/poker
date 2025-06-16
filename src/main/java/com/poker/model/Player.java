package com.poker.model;

import com.fasterxml.jackson.annotation.*;
import com.poker.model.GameSession;

import jakarta.persistence.*;

import java.util.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String phoneNumber;

    @Column(nullable = true)
    private Double finalAmount;

    // Player -> Withdrawal (One-to-Many)
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnoreProperties("player")
    private List<Withdrawal> withdrawals = new ArrayList<>();

    // Player -> Consumed Food (Many-to-Many)
    @JsonIgnore
    @ManyToMany(mappedBy = "consumers", fetch = FetchType.LAZY)
    private List<FoodExpense> consumedFood = new ArrayList<>();

    // Player -> Paid Food (One-to-Many)
    @JsonIgnore
    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    private List<FoodExpense> paidFood = new ArrayList<>();

    // Player -> Session (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private GameSession session;

    // --- Constructors ---

    public Player() {}

    public Player(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    // --- Getters and Setters ---

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

	public GameSession getSession() {
		return session;
	}

	public void setSession(GameSession session) {
		this.session = session;
	}

	public Double getFinalAmount() {
		return finalAmount;
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

	public List<FoodExpense> getConsumedFood() {
		return consumedFood;
	}

	public void setConsumedFood(List<FoodExpense> consumedFood) {
		this.consumedFood = consumedFood;
	}

	public List<FoodExpense> getPaidFood() {
		return paidFood;
	}

	public void setPaidFood(List<FoodExpense> paidFood) {
		this.paidFood = paidFood;
	}

	@Transient
	public double getTotalWithdrawals() {
	    return withdrawals != null
	        ? withdrawals.stream().mapToDouble(Withdrawal::getAmount).sum()
	        : 0.0;
	}
	
    
}
