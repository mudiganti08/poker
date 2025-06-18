package com.poker.service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poker.exception.BankMismatchException;
import com.poker.model.FoodExpense;
import com.poker.model.GameSession;
import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.repo.FoodExpenseRepository;
import com.poker.repo.PlayerRepository;
import com.poker.repo.WithdrawalRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private WithdrawalRepository withdrawalRepo;

    @Autowired
    private FoodExpenseRepository foodRepo;

    public List<Player> getAll() {
        return playerRepo.findAll();
    }

    public Player addPlayer(Player player, GameSession session) {
        player.setSession(session); // ✅ associate session
        return playerRepo.save(player);
    }

    public void deletePlayer(UUID id) {
        playerRepo.deleteById(id);
    }

    public void addWithdrawal(UUID playerId, Withdrawal withdrawal) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        withdrawal.setPlayer(player);
        withdrawalRepo.save(withdrawal);
    }

    public Player updateFinalAmount(UUID id, double amount) {
        Player player = playerRepo.findById(id).orElseThrow();
        player.setFinalAmount(amount);
        return playerRepo.save(player);
    }

    public Map<String, Object> calculateResults() {
        return calculateResultsInternal(playerRepo.findAll(), foodRepo.findAll());
    }

    public Map<String, Object> calculateResultsForSession(UUID sessionId) {
        List<Player> players = playerRepo.findPlayersBySessionId(sessionId);
        List<FoodExpense> foodExpenses = foodRepo.findAll(); // Optional: Filter if session-specific expenses

        return calculateResultsInternal(players, foodExpenses);
    }

    private void validateBankAmounts(double totalTaken, double totalReturned) {
        if (Math.abs(totalTaken - totalReturned) > 0.01) {
            throw new BankMismatchException(totalTaken, totalReturned);
        }
    }

    private Map<String, Object> calculateResultsInternal(List<Player> players, List<FoodExpense> foodExpenses) {
        Map<String, Double> pokerNet = new HashMap<>();
        Map<String, Double> foodNet = new HashMap<>();

        double totalTaken = 0.0;
        double totalReturned = 0.0;

        for (Player p : players) {
            List<Withdrawal> withdrawals = withdrawalRepo.findByPlayerId(p.getId());
            p.setWithdrawals(withdrawals);

            double taken = p.getTotalWithdrawals();
            double returned = Optional.ofNullable(p.getFinalAmount()).orElse(0.0);

            totalTaken += taken;
            totalReturned += returned;

            pokerNet.put(p.getName(), returned - taken);
            foodNet.put(p.getName(), 0.0);
        }

        for (FoodExpense fe : foodExpenses) {
            Player payer = fe.getPayer();
            if (payer == null || fe.getConsumers() == null || fe.getConsumers().isEmpty()) continue;

            String payerName = payer.getName();
            double total = fe.getAmount();
            int count = fe.getConsumers().size();
            double split = total / count;

            foodNet.put(payerName, foodNet.getOrDefault(payerName, 0.0) + total);

            for (Player consumer : fe.getConsumers()) {
                String name = consumer.getName();
                foodNet.put(name, foodNet.getOrDefault(name, 0.0) - split);
            }
        }

        Map<String, Double> finalNet = new HashMap<>();
        for (String name : pokerNet.keySet()) {
            double net = pokerNet.get(name) + foodNet.getOrDefault(name, 0.0);
            finalNet.put(name, net);
        }

        // Validate total amounts before proceeding
        validateBankAmounts(totalTaken, totalReturned);

        List<Map<String, Object>> settlements = calculateSettlements(finalNet);

        return Map.of(
        	    "pokerSummary", pokerNet,
        	    "foodSummary", foodNet,
        	    "finalSummary", finalNet,
        	    "settlements", settlements // now structured
        	);

    }

    private List<Map<String, Object>> calculateSettlements(Map<String, Double> netMap) {
        List<Map.Entry<String, Double>> creditors = new ArrayList<>();
        List<Map.Entry<String, Double>> debtors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : netMap.entrySet()) {
            if (entry.getValue() > 0.01) creditors.add(new AbstractMap.SimpleEntry<>(entry));
            else if (entry.getValue() < -0.01) debtors.add(new AbstractMap.SimpleEntry<>(entry));
        }

        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        debtors.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));

        List<Map<String, Object>> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            var debtor = debtors.get(i);
            var creditor = creditors.get(j);

            double amount = Math.min(-debtor.getValue(), creditor.getValue());

            Map<String, Object> settlement = new HashMap<>();
            settlement.put("from", debtor.getKey());
            settlement.put("to", creditor.getKey());
            settlement.put("amount", amount);
            settlements.add(settlement);

            debtor.setValue(debtor.getValue() + amount);
            creditor.setValue(creditor.getValue() - amount);

            if (Math.abs(debtor.getValue()) < 0.01) i++;
            if (Math.abs(creditor.getValue()) < 0.01) j++;
        }

        return settlements;
    }

    public List<Player> getAllPlayers() {
        List<Player> players = playerRepo.findAll();
        for (Player p : players) {
            List<Withdrawal> withdrawals = withdrawalRepo.findByPlayerId(p.getId());
            p.setWithdrawals(withdrawals);
            if (p.getFinalAmount() == null) {
                p.setFinalAmount(0.0);
            }
        }
        return players;
    }

    public Player findById(UUID id) {
        return playerRepo.findById(id).orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public void deleteExpense(UUID id) {
        foodRepo.deleteById(id);
    }

    public List<Player> getPlayersBySessionId(UUID sessionId) {
        return playerRepo.findPlayersBySessionId(sessionId);
    }
}
