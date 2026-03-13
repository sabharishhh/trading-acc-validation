package org.example.tradingaccountvalidation.controller;

import lombok.RequiredArgsConstructor;
import org.example.tradingaccountvalidation.model.BackupSnapshot;
import org.example.tradingaccountvalidation.repo.RuleBackupInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backups")
@CrossOrigin(origins = "http://localhost:3000") // Adjust to your React port
@RequiredArgsConstructor
public class RuleBackupController {

    private final RuleBackupInterface backupService;

    @GetMapping
    public ResponseEntity<List<BackupSnapshot>> getBackups() {
        return ResponseEntity.ok(backupService.listBackups());
    }

    @PostMapping("/create")
    public ResponseEntity<String> manualBackup(@RequestParam(defaultValue = "MANUAL") String action) {
        backupService.createBackup(action);
        return ResponseEntity.ok("Backup created successfully");
    }

    @PostMapping("/restore/{folderName}")
    public ResponseEntity<String> restoreBackup(@PathVariable String folderName) {
        try {
            backupService.restoreBackup(folderName);
            return ResponseEntity.ok("Restored successfully from " + folderName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Restore failed: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete/{folderName}")
    public ResponseEntity<String> deleteBackup(@PathVariable String folderName) {
        try {
            backupService.deleteBackup(folderName);
            return ResponseEntity.ok("Backup deleted successfully: " + folderName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }
}