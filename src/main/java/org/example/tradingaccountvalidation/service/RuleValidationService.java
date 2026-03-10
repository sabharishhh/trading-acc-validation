package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleValidationInterface;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RuleValidationService implements RuleValidationInterface {

    @Override
    public void validateDuplicateRuleTables(List<RuleMeta> rules, Set<String> uploadingFiles) {
        Map<String, String> tableOwners = new HashMap<>();

        for (RuleMeta rule : rules) {
            String table = rule.getRuleTableName().trim();
            String file = rule.getSourceFile();

            if (table == null || table.isBlank()) {
                throw new RuntimeException(
                        "RuleTable name missing in file: " + file
                );
            }

            if (tableOwners.containsKey(table)) {
                String existingFile = tableOwners.get(table);

                if (!existingFile.equals(file)) {

                    if (uploadingFiles.contains(existingFile) &&
                            uploadingFiles.contains(file)) {
                        continue;
                    }
                    throw new RuntimeException("Duplicate RuleTable detected: " + table + " in files " + existingFile + " and " + file);
                }
            }
            tableOwners.put(table, file);
        }
    }
}