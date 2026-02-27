package org.example.tradingaccountvalidation.controller;

import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleStorageInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rules")
public class RuleUploadController {

    private final RuleStorageInterface storage;
    private final RuleEngineInterface engine;

    public RuleUploadController(RuleStorageInterface storage,
                                RuleEngineInterface engine) {
        this.storage = storage;
        this.engine = engine;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file) {

        storage.validate(file);
        storage.save(file);
        engine.reloadRules();

        return ResponseEntity.ok(
                "File uploaded and rules reloaded"
        );
    }
}