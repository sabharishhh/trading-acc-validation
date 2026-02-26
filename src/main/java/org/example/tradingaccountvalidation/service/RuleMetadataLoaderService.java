package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tradingaccountvalidation.model.ConditionMeta;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class RuleMetadataLoaderService implements RuleMetadataLoaderInterface {

    private final List<RuleMeta> allRules = new ArrayList<>();

    @PostConstruct
    public void load() throws Exception {

        InputStream is =
                new ClassPathResource("rules/rules_dynamic_test.xlsx")
                        .getInputStream();

        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        // 🔥 Dynamically find JSON path header row
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

        if (header == null) {
            throw new RuntimeException("JSON path header row not found");
        }

        Map<Integer, String> columnPathMap = new HashMap<>();

        for (Cell cell : header) {
            String headerValue = getCellValue(cell);
            if (headerValue != null && headerValue.startsWith("/account/")) {
                columnPathMap.put(cell.getColumnIndex(), headerValue);
            }
        }

        // 🔥 Load rule rows (skip everything above header)
        for (int i = header.getRowNum() + 2; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            String ruleId = getCellValue(row.getCell(0));
            if (ruleId == null || ruleId.isBlank()) continue;

            String agenda = getCellValue(row.getCell(1));
            String from   = getCellValue(row.getCell(2));
            String to     = getCellValue(row.getCell(3));

            RuleMeta meta =
                    new RuleMeta(ruleId, agenda, from, to);

            for (Map.Entry<Integer, String> entry : columnPathMap.entrySet()) {

                Cell conditionCell =
                        row.getCell(entry.getKey());

                String expected = getCellValue(conditionCell);

                if (expected != null && !expected.isBlank()) {
                    meta.getConditions().add(
                            new ConditionMeta(
                                    entry.getValue(),
                                    expected
                            )
                    );
                }
            }

            allRules.add(meta);
        }

        workbook.close();
        is.close();
    }

    @Override
    public List<RuleMeta> getByTransition(String from, String to) {

        return allRules.stream()
                .filter(r ->
                        Objects.equals(r.getStatusFrom(), from) &&
                                Objects.equals(r.getStatusTo(), to)
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