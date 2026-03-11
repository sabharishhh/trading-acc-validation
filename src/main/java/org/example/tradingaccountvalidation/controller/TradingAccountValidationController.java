package org.example.tradingaccountvalidation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.repo.TradingAccountValidationInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validate")
public class TradingAccountValidationController {
    private static final Logger log = LoggerFactory.getLogger(TradingAccountValidationController.class);

    @Autowired
    private TradingAccountValidationInterface service;

    @PostMapping("/user")
    public ResponseEntity<DynamicAccountSnapshot> validateAccount(@RequestBody JsonNode body) {
        DynamicAccountSnapshot snapshot = new DynamicAccountSnapshot(body);
        String customerId = snapshot.getString("/account/customerId");

        log.info("Account validation requested for id: {}", customerId);

        try {
            DynamicAccountSnapshot updatedSnapshot = service.validateAccount(snapshot);
            return ResponseEntity.ok(updatedSnapshot);
        } catch (Exception e) {
            log.error("Validation error for id: {}", customerId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}