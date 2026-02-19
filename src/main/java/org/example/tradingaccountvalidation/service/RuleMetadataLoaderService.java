package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.tradingaccountvalidation.model.ConditionMetadata;
import org.example.tradingaccountvalidation.model.RuleMetadata;
import org.example.tradingaccountvalidation.repo.MetadataLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleMetadataLoaderService implements MetadataLoader {

    private Map<String, List<RuleMetadata>> rulesByAgenda = new HashMap<>();

    @PostConstruct
    public void load() throws Exception {

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("rules/rules_dynamic.xlsx");

        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);

        int headerRowIndex = 7;  // adjust based on your sheet layout
        int firstDataRow = 10;

        for (int i = firstDataRow; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            String ruleName = row.getCell(0).getStringCellValue();
            String agenda = row.getCell(1).getStringCellValue();

            List<ConditionMetadata> conditions = new ArrayList<>();

            // Now parse condition columns.
            // Example:
            // statusFrom column at index 2
            String statusFrom = row.getCell(2).getStringCellValue();
            conditions.add(new ConditionMetadata(
                    "/account/statusFrom",
                    statusFrom
            ));

            // statusTo column at index 3
            String statusTo = row.getCell(3).getStringCellValue();
            conditions.add(new ConditionMetadata(
                    "/account/statusTo",
                    statusTo
            ));

            // equityOpenPosition column at index 4
            String equity = row.getCell(4).getStringCellValue();
            conditions.add(new ConditionMetadata(
                    "/account/equityOpenPosition",
                    equity
            ));

            RuleMetadata ruleMeta =
                    new RuleMetadata(ruleName, agenda, conditions);

            rulesByAgenda
                    .computeIfAbsent(agenda, k -> new ArrayList<>())
                    .add(ruleMeta);
        }
    }

    public List<RuleMetadata> getRulesForAgenda(String agenda) {
        return rulesByAgenda.getOrDefault(agenda, List.of());
    }
}