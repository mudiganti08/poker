package com.poker.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.poker.model.FoodExpense;
import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.repo.FoodExpenseRepository;
import com.poker.repo.PlayerRepository;
import com.poker.repo.WithdrawalRepository;

@Service
public class BackupService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private FoodExpenseRepository foodExpenseRepository;

    private static final String BACKUP_FOLDER = "C:\\Backup";
    private static final DateTimeFormatter FILE_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private String lastPlayerDataHash = "";
    private String lastExpenseDataHash = "";

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Scheduled(fixedRate = 1 * 60 * 1000) // every 1 minute
    public void exportSummaryCSV() {
        new File(BACKUP_FOLDER).mkdirs();

        executor.submit(() -> {
            try {
                exportPlayerBankSummaryIfChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            try {
                exportFoodExpensesIfChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void exportPlayerBankSummaryIfChanged() throws IOException {
        List<Player> players = playerRepository.findAll();
        if (players.isEmpty()) return;

        StringBuilder sb = new StringBuilder();

        for (Player player : players) {
            double takenFromBank = player.getWithdrawals()
                    .stream()
                    .mapToDouble(Withdrawal::getAmount)
                    .sum();

            double returnedToBank = player.getFinalAmount() != null ? player.getFinalAmount() : 0.0;

            sb.append(escapeCsv(player.getName())).append(",")
              .append(takenFromBank).append(",")
              .append(returnedToBank).append("\n");
        }

        String currentHash = hash(sb.toString());

        if (!currentHash.equals(lastPlayerDataHash)) {
            lastPlayerDataHash = currentHash;
            String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
            String filePath = BACKUP_FOLDER + "\\player_bank_summary_" + timestamp + ".csv";

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write("PlayerName,AmountTakenFromBank,AmountReturnedToBank\n");
                writer.write(sb.toString());
            }
        }
    }

    private void exportFoodExpensesIfChanged() throws IOException {
        List<FoodExpense> expenses = foodExpenseRepository.findAll();
        if (expenses.isEmpty()) return;

        StringBuilder sb = new StringBuilder();

        for (FoodExpense expense : expenses) {
            String payer = escapeCsv(expense.getPayer().getName());
            String consumers = expense.getConsumers()
                    .stream()
                    .map(p -> escapeCsv(p.getName()))
                    .collect(Collectors.joining(", "));

            sb.append(expense.getAmount()).append(",")
              .append(payer).append(",")
              .append(consumers).append("\n");
        }

        String currentHash = hash(sb.toString());

        if (!currentHash.equals(lastExpenseDataHash)) {
            lastExpenseDataHash = currentHash;
            String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
            String filePath = BACKUP_FOLDER + "\\food_expenses_" + timestamp + ".csv";

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write("Amount,PayerName,ConsumerNames\n");
                writer.write(sb.toString());
            }
        }
    }

    private String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash data", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
