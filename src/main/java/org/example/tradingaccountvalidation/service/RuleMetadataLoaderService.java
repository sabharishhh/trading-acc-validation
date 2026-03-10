package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tradingaccountvalidation.model.ConditionMeta;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.model.RuleTableRow;
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

        if (files == null) {
            return;
        }

        for (File file : files) {
            allRules.addAll(loadFileMetadata(file));
        }
    }

    public List<RuleMeta> loadFromFiles(File[] files) throws Exception {
        List<RuleMeta> extracted = new ArrayList<>();

        if (files == null) return extracted;

        for (File file : files) {
            extracted.addAll(loadFileMetadata(file));
        }
        return extracted;
    }

    private List<RuleMeta> loadFileMetadata(File file) throws Exception {
        List<RuleMeta> extracted = new ArrayList<>();

        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) return extracted;

            Row header = null;
            String ruleTableName = null;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);

                if (r == null) continue;

                for (Cell c : r) {
                    String val = getCellValue(c);

                    if (val == null) continue;

                    String normalized = val.trim();

                    if (normalized.startsWith("RuleTable")) {
                        ruleTableName = normalized.replace("RuleTable", "").trim();
                    }

                    if (normalized.startsWith("/account/")) {
                        header = r;
                        break;
                    }
                }
                if (header != null) break;
            }

            if (header == null) return extracted;

            if (ruleTableName == null || ruleTableName.isBlank()) {
                ruleTableName = file.getName();
            }

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

                RuleMeta meta = new RuleMeta(
                        ruleId,
                        ruleTableName,
                        agenda,
                        from,
                        to,
                        file.getName()
                );

                for (Map.Entry<Integer, String> entry : columnPathMap.entrySet()) {
                    Cell conditionCell = row.getCell(entry.getKey());
                    String expected = getCellValue(conditionCell);

                    if (expected != null && !expected.isBlank()) {
                        String template = columnDescriptionMap.get(entry.getKey());
                        meta.getConditions().add(
                                new ConditionMeta(
                                        entry.getValue(),
                                        expected,
                                        template
                                )
                        );
                    }
                }
                extracted.add(meta);
            }
        }
        return extracted;
    }

    @Override
    public List<RuleTableRow> getRuleTable() {
        List<RuleTableRow> result = new ArrayList<>();

        for (RuleMeta rule : allRules) {
            Map<String, String> conditionMap = new LinkedHashMap<>();

            for (ConditionMeta condition : rule.getConditions()) {
                String key = condition.path().replace("/account/", "");
                conditionMap.put(key, condition.expected());
            }

            result.add(
                    new RuleTableRow(
                            rule.getRuleId(),
                            rule.getStatusFrom(),
                            rule.getStatusTo(),
                            rule.getSourceFile(),
                            conditionMap
                    )
            );
        }
        return result;
    }

    @Override
    public List<RuleMeta> getByTransition(String from, String to) {

        return allRules.stream()
                .filter(r ->
                        Objects.equals(r.getStatusFrom(), from)
                                && Objects.equals(r.getStatusTo(), to)
                ).toList();
    }

    @Override
    public List<RuleMeta> getAllRules() {
        return new ArrayList<>(allRules);
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