package com.poker.controller;

import com.poker.model.FoodExpense;
import com.poker.model.Player;
import com.poker.model.Withdrawal;
import com.poker.repo.FoodExpenseRepository;
import com.poker.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class PlayerController {

	@Autowired
	private PlayerService service;
	
	@Autowired
	private FoodExpenseRepository foodRepo;

	@GetMapping("/test")
	public String check() {
		return "Nee Gudda";
	}

	@GetMapping("/food")
	public List<FoodExpense> getFoodExpenses() {
		return foodRepo.findAll();
	}

	@GetMapping("/players")
	public List<Player> getAll() {
		return service.getAllPlayers();
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
	    public ResponseEntity<?> calculateResults(@RequestParam(defaultValue = "false") boolean override) {
	        double totalTaken = service.getAllPlayers().stream()
	                .mapToDouble(Player::getTotalWithdrawals).sum();

	        double totalReturned = service.getAllPlayers().stream()
	                .mapToDouble(Player::getFinalAmount).sum();


	        return ResponseEntity.ok(service.calculateResults());
	    }
	 @PostMapping("/food")
	 public FoodExpense addFood(@RequestBody FoodExpense food) {
	     UUID payerId = food.getPayer().getId();
	     Player payer = service.findById(payerId);

	     List<Player> fullConsumers = new ArrayList<>();
	     for (Player c : food.getConsumers()) {
	         fullConsumers.add(service.findById(c.getId()));
	     }

	     food.setPayer(payer);
	     food.setConsumers(fullConsumers);

	     return foodRepo.save(food);
	 }
	 @DeleteMapping("/food/{id}")
	 public ResponseEntity<Void> deleteFoodExpense(@PathVariable UUID id) {
		 service.deletePlayer(id);
	     return ResponseEntity.noContent().build();
	 }


}
