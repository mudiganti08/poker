package com.poker.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poker.model.FoodExpense;

public interface FoodExpenseRepository extends JpaRepository<FoodExpense, UUID> {
}
