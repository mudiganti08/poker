package com.poker.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BankMismatchException extends ResponseStatusException {
    public BankMismatchException(double totalTaken, double totalReturned) {
        super(HttpStatus.BAD_REQUEST, 
              String.format("⚠️ Bank mismatch! Total taken: $%.2f, returned: $%.2f", totalTaken, totalReturned));
    }
}

