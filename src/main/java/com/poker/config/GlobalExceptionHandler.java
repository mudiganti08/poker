package com.poker.config;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.poker.exception.BankMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BankMismatchException.class)
    public ResponseEntity<Object> handleBankMismatchException(BankMismatchException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bank Mismatch");
        body.put("message", ex.getReason());
        body.put("path", "/api/settle");  // optionally detect dynamically from request

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // You can add more @ExceptionHandler for other exceptions here if needed
}

