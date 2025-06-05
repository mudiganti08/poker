package com.poker.service;

import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.repo.PlayerRepository;
import com.poker.repo.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private WithdrawalRepository withdrawalRepo;

    public List<Player> getAll() {
        return playerRepo.findAll();
    }

    public Player addPlayer(Player p) {
        Player newPlayer = playerRepo.save(p);
        Withdrawal initial = new Withdrawal();
        initial.setAmount(20);
        initial.setAddedBy("System");
        initial.setPlayer(newPlayer);
        withdrawalRepo.save(initial);
        return newPlayer;
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
        Player p = playerRepo.findById(id).orElseThrow();
        p.setFinalAmount(amount);
        return playerRepo.save(p);
    }

    public Map<String, Object> calculateResults() {
        List<Player> players = playerRepo.findAll();
        Map<String, Double> netMap = new HashMap<>();

        for (Player p : players) {
            double taken = p.getTotalWithdrawals();
            double returned = p.getFinalAmount();
            double net = returned - taken;
            netMap.put(p.getName(), net);
        }

        List<Map.Entry<String, Double>> creditors = new ArrayList<>(netMap.entrySet());
        List<Map.Entry<String, Double>> debtors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : netMap.entrySet()) {
            if (entry.getValue() > 0.01) {
                creditors.add(entry);
            } else if (entry.getValue() < -0.01) {
                debtors.add(entry);
            }
        }


        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        debtors.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));

        List<String> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<String, Double> debtor = debtors.get(i);
            Map.Entry<String, Double> creditor = creditors.get(j);

            double amount = Math.min(-debtor.getValue(), creditor.getValue());

            settlements.add(String.format("%s has to pay $%.2f to %s",
                    debtor.getKey(), amount, creditor.getKey()));

            debtor.setValue(debtor.getValue() + amount);
            creditor.setValue(creditor.getValue() - amount);

            if (Math.abs(debtor.getValue()) < 0.01) i++;
            if (Math.abs(creditor.getValue()) < 0.01) j++;
        }

        return Map.of("summary", netMap, "settlements", settlements);
    }
}
