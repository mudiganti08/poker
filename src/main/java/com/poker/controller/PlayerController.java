package com.poker.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poker.dto.SessionRequest;
import com.poker.model.FoodExpense;
import com.poker.model.GameSession;
import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.repo.FoodExpenseRepository;
import com.poker.repo.PlayerRepository;
import com.poker.repo.WithdrawalRepository;
import com.poker.service.PlayerService;
import com.poker.service.SessionService;
import com.poker.service.TwilioService;

@RestController
@RequestMapping("/api")
public class PlayerController {

    @Autowired private PlayerService service;
    @Autowired private FoodExpenseRepository foodRepo;
    @Autowired private SessionService sessionService;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private WithdrawalRepository withdrawalRepository;

    @PostMapping("/session")
    public ResponseEntity<String> startSession(@RequestBody SessionRequest req) {
    	if(!sessionService.sessionExists()) {
        GameSession session = sessionService.startSession(req.getDate());

        List<Map<String, String>> playersData = List.of(
            Map.of("name", "Sridhar", "phone", "+16039213404"),
            Map.of("name", "Santosh", "phone", "+16039213404")


            // Add more players as needed
        );

        for (Map<String, String> data : playersData) {
            Player p = new Player();
            p.setName(data.get("name"));
            p.setPhoneNumber(data.get("phone"));
            p.setSession(session);
            p = service.addPlayer(p, session);

            Withdrawal w = new Withdrawal();
            w.setAmount(20.0);
            w.setPlayer(p);
            service.addWithdrawal(p.getId(), w);
            List<Withdrawal> players = withdrawalRepository.findByPlayerId(p.getId());

            double totalWithdrawals = players.get(0).getAmount();
                   
            String message = String.format(
                "Hi %s, your initial withdrawal is $%.2f. Total withdrawals so far: $%.2f.",
                p.getName(), w.getAmount(), totalWithdrawals
            );

            sendWhatsApp(p.getPhoneNumber(), message);
        }
    	}

        return ResponseEntity.ok("Session started and SMS sent to all players.");
    }

    @GetMapping("/test")
    public String check() {
        return "HI kOTHI GUDDA .....";
    }

    @GetMapping("/food")
    public List<FoodExpense> getFoodExpenses() {
        return foodRepo.findAll();
    }

    @GetMapping("/players")
    public List<Player> getAllPlayers() {
        return service.getAllPlayers();
    }

    @PostMapping("/players")
    public Player addPlayer(@RequestBody Player p) {
        // Fetch the latest session
        Optional<GameSession> sessionOpt = sessionService.getLatestSessionOptional();
        
        if (sessionOpt.isEmpty()) {
            throw new IllegalStateException("No active game session found. Start a session first.");
        }

        GameSession session = sessionOpt.get();

        // Attach session to player before saving
        Withdrawal w = new Withdrawal();
        w.setAmount(20.0);
        w.setPlayer(p);
        p = service.addPlayer(p, session);
        service.addWithdrawal(p.getId(), w);
        List<Withdrawal> players = withdrawalRepository.findByPlayerId(p.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        String formattedTime = w.getTimestamp() != null
                ? w.getTimestamp().atZone(ZoneId.systemDefault()).format(formatter)
                : "Unknown Time";
        double totalWithdrawals = players.get(0).getAmount();
               
        String message = String.format(
            "Hi %s, your initial withdrawal is $%.2f. Total withdrawals so far: $%.2f.",
            p.getName(), w.getAmount(), totalWithdrawals, formattedTime
        );
        sendWhatsApp("+16039213404", message);
        return p;
    }

    @DeleteMapping("/players/{id}")
    public void deletePlayer(@PathVariable UUID id) {
        service.deletePlayer(id);
    }

    @PostMapping("/players/{id}/withdrawals")
    public void addWithdrawal(@PathVariable UUID id, @RequestBody Map<String, Object> payload) {
        double amount = Double.parseDouble(payload.get("amount").toString());
        String addedBy = (String) payload.getOrDefault("addedBy", "Unknown");

        Withdrawal w = new Withdrawal();
        w.setAmount(amount);
        w.setAddedBy(addedBy); // Make sure this field exists in your model

        service.addWithdrawal(id, w);
        Player p = service.findById(id);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        String formattedTime = w.getTimestamp() != null
                ? w.getTimestamp().atZone(ZoneId.systemDefault()).format(formatter)
                : "Unknown Time";

        List<Withdrawal> withdrawals = withdrawalRepository.findByPlayerId(p.getId());
        double totalWithdrawals = withdrawals.stream().mapToDouble(Withdrawal::getAmount).sum();

        if (p.getPhoneNumber() != null && !p.getPhoneNumber().isEmpty()) {
            sendWhatsApp(
                p.getPhoneNumber(),
                String.format(
                    "Hi %s, $%.2f added to your account at %s by %s.\nTotal withdrawals so far: $%.2f.",
                    p.getName(), w.getAmount(), formattedTime, addedBy, totalWithdrawals
                )
            );
        }
    }
    @PutMapping("/players/{id}")
    public Player updateFinalAmount(@PathVariable UUID id, @RequestBody Player updated) {
        return service.updateFinalAmount(id, updated.getFinalAmount());
    }

//    @GetMapping("/settle")
//    public ResponseEntity<?> calculateResults(@RequestParam(defaultValue = "false") boolean override) {
//        Optional<GameSession> sessionOpt = sessionService.getLatestSessionOptional();
//
//        if (sessionOpt.isEmpty()) {
//            return ResponseEntity.badRequest().body("No active session found.");
//        }
//
//        GameSession session = sessionOpt.get();
//       Map<String, Object> results = service.calculateResultsForSession(session.getId());
//        return ResponseEntity.ok(results);
//    }

   

    @PostMapping("/food")
    public FoodExpense addFoodExpense(@RequestBody FoodExpense food) {
        if (food == null || food.getPayer() == null || food.getPayer().getId() == null) {
            throw new IllegalArgumentException("Payer must be provided with a valid ID");
        }

        UUID payerId = food.getPayer().getId();
        Player payer = service.findById(payerId);

        if (food.getConsumers() == null || food.getConsumers().isEmpty()) {
            throw new IllegalArgumentException("At least one consumer must be provided");
        }

        List<Player> fullConsumers = new ArrayList<>();
        for (Player c : food.getConsumers()) {
            if (c == null || c.getId() == null) {
                throw new IllegalArgumentException("Each consumer must have a valid ID");
            }
            Player consumer = service.findById(c.getId());
            fullConsumers.add(consumer);
        }

        food.setPayer(payer);
        food.setConsumers(fullConsumers);

        return foodRepo.save(food);
    }

    @DeleteMapping("/food/{id}")
    public ResponseEntity<Void> deleteFoodExpense(@PathVariable UUID id) {
        foodRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session/current")
    public ResponseEntity<?> getCurrentSessionPlayers() {
        Optional<GameSession> session = sessionService.getLatestSessionOptional();
        if (session.isPresent()) {
            List<Player> players = playerRepository.findBySession(session.get());
            return ResponseEntity.ok(players);
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    @PostMapping("/players/{id}/return")
    public ResponseEntity<Void> updateReturnAmount(@PathVariable UUID id, @RequestBody Map<String, Double> payload) {
        if (!payload.containsKey("amount")) {
            return ResponseEntity.badRequest().build();
        }
        double amount = payload.get("amount");
        service.updateFinalAmount(id, amount);
        return ResponseEntity.ok().build();
    }


    public void sendWhatsApp(String phoneNumber, String message) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://api.callmebot.com/whatsapp.php")
                    .queryParam("phone", phoneNumber)
                    .queryParam("text", message)
                    .queryParam("apikey", "4545437")
                    .build()
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("WhatsApp sent to " + phoneNumber + ": " + response);
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp to " + phoneNumber + ": " + e.getMessage());
        }
    }
    


    @GetMapping("/settle")
    public ResponseEntity<?> calculateResults(@RequestParam(defaultValue = "false") boolean override) {
        Optional<GameSession> sessionOpt = sessionService.getLatestSessionOptional();

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No active session found.");
        }

        GameSession session = sessionOpt.get();
        Map<String, Object> results = service.calculateResultsForSession(session.getId());

        // Build the settlements WhatsApp message if available
        if (results.containsKey("settlements")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> settlements = (List<Map<String, Object>>) results.get("settlements");

            StringBuilder sb = new StringBuilder();
            sb.append("💰 *Poker Game Settlements Summary:*\n\n");

            for (Map<String, Object> settlement : settlements) {
                String from = settlement.get("from") != null ? settlement.get("from").toString() : "Unknown";
                String to = settlement.get("to") != null ? settlement.get("to").toString() : "Unknown";

                double amount = 0.0;
                try {
                    Object amtObj = settlement.get("amount");
                    if (amtObj instanceof Number) {
                        amount = ((Number) amtObj).doubleValue();
                    } else if (amtObj instanceof String) {
                        amount = Double.parseDouble((String) amtObj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sb.append(String.format("➡️ %s pays $%.2f to %s\n", from, amount, to));
            }

            sb.append("\n✔️ Please settle up as soon as possible!");

            // Send the WhatsApp message
            sendWhatsApp("+16039213404", sb.toString());
        }

        return ResponseEntity.ok(results);
    }

} 
