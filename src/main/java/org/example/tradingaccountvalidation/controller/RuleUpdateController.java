package org.example.tradingaccountvalidation.controller;

import lombok.RequiredArgsConstructor;
import org.example.tradingaccountvalidation.model.RuleUpdateRequest;
import org.example.tradingaccountvalidation.repo.RuleBackupInterface;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.service.AuditService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class RuleUpdateController {
    private final RuleEngineInterface ruleEngine;
    private final RuleMetadataLoaderInterface metadataLoader;
    private final RuleBackupInterface backupService;
    private final AuditService auditService;

    @Value("${rules.folder}")
    private String rulesFolder;

    @PostMapping("/update")
    public ResponseEntity<String> updateRules(@RequestBody RuleUpdateRequest request) {
        backupService.createBackup("CELL_UPDATE_" + request.getFileName());

        File file = new File(rulesFolder, request.getFileName());

        if (!file.exists()) {
            return ResponseEntity.badRequest().body("File not found: " + request.getFileName());
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            Row headerRow = null;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r != null && r.getCell(0) != null) {
                    if (r.getCell(0).getStringCellValue().equalsIgnoreCase("Name") ||
                            r.getCell(0).getStringCellValue().equalsIgnoreCase("Rule Name")) {
                        headerRow = sheet.getRow(i + 2);
                        break;
                    }
                }
            }

            if (headerRow == null) throw new RuntimeException("Could not locate condition paths row");

            Map<String, Integer> pathToColIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue();
                    if (val.contains("/account/")) {
                        int start = val.indexOf("/account/") + 9;
                        int end = val.indexOf("\"", start);
                        if (end > start) {
                            String key = val.substring(start, end);
                            pathToColIndex.put(key, cell.getColumnIndex());
                        }
                    }
                }
            }

            for (Map.Entry<String, Map<String, String>> ruleUpdate : request.getUpdates().entrySet()) {
                String targetRuleId = ruleUpdate.getKey();
                Map<String, String> cellChanges = ruleUpdate.getValue();

                for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) continue;

                    String currentRuleId = dataFormatter.formatCellValue(row.getCell(0)).trim();
                    if (targetRuleId.equals(currentRuleId)) {

                        for (Map.Entry<String, String> change : cellChanges.entrySet()) {
                            Integer colIdx = pathToColIndex.get(change.getKey());
                            if (colIdx != null) {
                                Cell cellToUpdate = row.getCell(colIdx);
                                String oldValue = "-";

                                if (cellToUpdate == null) {
                                    cellToUpdate = row.createCell(colIdx);
                                } else {
                                    oldValue = dataFormatter.formatCellValue(cellToUpdate);
                                }

                                String newValue = change.getValue();

                                if (!oldValue.equals(newValue)) {
                                    cellToUpdate.setCellValue(newValue);
                                    auditService.logEdit(request.getFileName(), targetRuleId, change.getKey(), oldValue, newValue);
                                }
                            }
                        }
                        break;
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            ruleEngine.reloadRules();
            metadataLoader.reload();

            return ResponseEntity.ok("Rules updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating rules: " + e.getMessage());
        }
    }

    @GetMapping("/audit")
    public ResponseEntity<List<Map<String, String>>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}