package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tradingaccountvalidation.model.ConditionMeta;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.model.RuleTableRow;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Component
public class RuleMetadataLoaderService implements RuleMetadataLoaderInterface {
    private static final Logger log = LoggerFactory.getLogger(RuleMetadataLoaderService.class);

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

        if (!folder.exists() || !folder.isDirectory()) {
            log.error("Rules folder does not exist or is not a directory: {}", rulesFolderPath);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx") && !name.startsWith("~$"));
        if (files == null || files.length == 0) return;

        for (File file : files) {
            allRules.addAll(loadFileMetadata(file));
        }
        log.info("Total rules successfully loaded into metadata cache: {}", allRules.size());
    }

    public List<RuleMeta> loadFromFiles(File[] files) throws Exception {
        List<RuleMeta> extracted = new ArrayList<>();
        if (files == null) return extracted;
        for (File file : files) {
            if (!file.getName().startsWith("~$")) {
                extracted.addAll(loadFileMetadata(file));
            }
        }
        return extracted;
    }

    private List<RuleMeta> loadFileMetadata(File file) throws Exception {
        List<RuleMeta> extracted = new ArrayList<>();

        try (InputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return extracted;

            Row typeRow = null;
            int typeRowIndex = -1;
            String ruleTableName = file.getName();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String firstCell = getCellValue(r.getCell(0));
                if (firstCell != null && firstCell.trim().startsWith("RuleTable")) {
                    ruleTableName = firstCell.replace("RuleTable", "").trim();
                }

                if (firstCell != null && (firstCell.equalsIgnoreCase("Name") || firstCell.equalsIgnoreCase("Rule Name"))) {
                    typeRow = r;
                    typeRowIndex = i;
                    break;
                }
            }

            if (typeRow == null) {
                log.error("Could not find the Type row (Name, Condition, Action) in file: {}", file.getName());
                return extracted;
            }

            Set<Integer> conditionColumnIndexes = new HashSet<>();
            for (Cell cell : typeRow) {
                String val = getCellValue(cell);
                if (val != null && val.trim().equalsIgnoreCase("Condition")) {
                    conditionColumnIndexes.add(cell.getColumnIndex());
                }
            }

            Row conditionPathRow = null;
            for (int i = typeRowIndex + 1; i <= typeRowIndex + 5; i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                for (Integer colIdx : conditionColumnIndexes) {
                    String val = getCellValue(r.getCell(colIdx));
                    if (val != null && val.contains("/account/")) {
                        conditionPathRow = r;
                        break;
                    }
                }
                if (conditionPathRow != null) break;
            }

            if (conditionPathRow == null) return extracted;

            Map<Integer, String> columnPathMap = new HashMap<>();
            for (Integer colIdx : conditionColumnIndexes) {
                String val = getCellValue(conditionPathRow.getCell(colIdx));
                if (val != null && val.contains("/account/")) {
                    columnPathMap.put(colIdx, extractPath(val));
                }
            }

            Row descriptionRow = sheet.getRow(conditionPathRow.getRowNum() + 1);
            Map<Integer, String> columnDescriptionMap = new HashMap<>();
            if (descriptionRow != null) {
                for (Integer colIdx : conditionColumnIndexes) {
                    String desc = getCellValue(descriptionRow.getCell(colIdx));
                    if (desc != null && !desc.isBlank()) {
                        columnDescriptionMap.put(colIdx, desc);
                    }
                }
            }

            for (int i = conditionPathRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String ruleId = getCellValue(row.getCell(0));
                if (ruleId == null || ruleId.isBlank() ||
                        ruleId.equalsIgnoreCase("Description") ||
                        ruleId.equalsIgnoreCase("Name")) {
                    continue;
                }

                String agenda = getCellValue(row.getCell(1));
                if (agenda != null) agenda = agenda.replace("\"", "").trim();

                RuleMeta meta = new RuleMeta(ruleId, ruleTableName, agenda, file.getName());

                for (Map.Entry<Integer, String> entry : columnPathMap.entrySet()) {
                    int colIdx = entry.getKey();
                    String path = entry.getValue();

                    Cell cell = row.getCell(colIdx);
                    String expected = getCellValue(cell);

                    if (expected != null && !expected.isBlank()) {
                        expected = expected.replace("\"", "").trim();
                        String template = columnDescriptionMap.get(colIdx);
                        meta.getConditions().add(new ConditionMeta(path, expected, template));
                    }
                }
                extracted.add(meta);
            }
        } catch (Exception e) {
            log.error("Error parsing Excel file: {}", e.getMessage(), e);
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
            result.add(new RuleTableRow(rule.getRuleId(), rule.getAgendaGroup(), rule.getSourceFile(), conditionMap));
        }
        return result;
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
            case FORMULA -> {
                try {
                    yield cell.getRichStringCellValue().getString();
                } catch (Exception e) {
                    yield cell.getCellFormula();
                }
            }
            default -> null;
        };
    }

    private String extractPath(String expression) {
        int start = expression.indexOf("/account/");
        if (start < 0) return expression;
        int end = expression.indexOf("\"", start);
        if (end < 0) return expression;
        return expression.substring(start, end);
    }

    @Override
    public List<RuleMeta> getByAgendaGroup(String agenda) {
        return allRules.stream()
                .filter(r -> r.getAgendaGroup() != null && r.getAgendaGroup().equals(agenda))
                .toList();
    }
}