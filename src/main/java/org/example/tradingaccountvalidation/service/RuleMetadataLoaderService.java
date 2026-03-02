package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tradingaccountvalidation.model.ConditionMeta;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Component
public class RuleMetadataLoaderService implements RuleMetadataLoaderInterface {

    @Value("${rules.folder}")
    private String rulesFolderPath;

    private final List<RuleMeta> allRules = new ArrayList<>();

    @PostConstruct
    public void init() throws Exception {
        reload();
    }

    @Override
    public synchronized void reload() throws Exception {

        allRules.clear();

        File folder = new File(rulesFolderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!folder.isDirectory()) {
            throw new RuntimeException("Rules folder is not a directory: " + rulesFolderPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            loadFile(file);
        }
    }

    private void loadFile(File file) throws Exception {

        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return;

            Row header = null;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {

                Row r = sheet.getRow(i);
                if (r == null) continue;

                for (Cell c : r) {
                    String val = getCellValue(c);
                    if (val != null && val.startsWith("/account/")) {
                        header = r;
                        break;
                    }
                }
                if (header != null) break;
            }

            if (header == null) return;

            Map<Integer, String> columnPathMap = new HashMap<>();

            for (Cell cell : header) {
                String headerValue = getCellValue(cell);
                if (headerValue != null && headerValue.startsWith("/account/")) {
                    columnPathMap.put(cell.getColumnIndex(), headerValue);
                }
            }

            Row descriptionRow = sheet.getRow(header.getRowNum() + 1);
            Map<Integer, String> columnDescriptionMap = new HashMap<>();

            if (descriptionRow != null) {
                for (Cell cell : descriptionRow) {
                    String description = getCellValue(cell);
                    if (description != null && !description.isBlank()) {
                        columnDescriptionMap.put(cell.getColumnIndex(), description);
                    }
                }
            }

            for (int i = header.getRowNum() + 2; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String ruleId = getCellValue(row.getCell(0));
                if (ruleId == null || ruleId.isBlank()) continue;

                String agenda = getCellValue(row.getCell(1));
                String from = getCellValue(row.getCell(2));
                String to = getCellValue(row.getCell(3));

                RuleMeta meta = new RuleMeta(ruleId, agenda, from, to);

                for (Map.Entry<Integer, String> entry : columnPathMap.entrySet()) {

                    Cell conditionCell = row.getCell(entry.getKey());
                    String expected = getCellValue(conditionCell);

                    if (expected != null && !expected.isBlank()) {
                        String template = columnDescriptionMap.get(entry.getKey());
                        meta.getConditions().add(
                                new ConditionMeta(entry.getValue(), expected, template)
                        );
                    }
                }

                allRules.add(meta);
            }
        }
    }

    @Override
    public List<RuleMeta> getByTransition(String from, String to) {

        return allRules.stream()
                .filter(r ->
                        Objects.equals(r.getStatusFrom(), from)
                                && Objects.equals(r.getStatusTo(), to)
                )
                .toList();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> null;
        };
    }
}