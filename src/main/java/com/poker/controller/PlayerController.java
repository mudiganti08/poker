package com.poker.controller;

import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService service;

    @GetMapping("/players")
    public List<Player> getAll() {
        return service.getAll();
    }

    @PostMapping("/players")
    public Player add(@RequestBody Player p) {
        return service.addPlayer(p);
    }

    @DeleteMapping("/players/{id}")
    public void delete(@PathVariable UUID id) {
        service.deletePlayer(id);
    }

    @PostMapping("/players/{id}/withdrawals")
    public void addWithdrawal(@PathVariable UUID id, @RequestBody Withdrawal w) {
        service.addWithdrawal(id, w);
    }

    @PutMapping("/players/{id}")
    public Player updateFinalAmount(@PathVariable UUID id, @RequestBody Player updated) {
        return service.updateFinalAmount(id, updated.getFinalAmount());
    }

    @GetMapping("/results")
    public Map<String, Object> results() {
        return service.calculateResults();
    }
}
