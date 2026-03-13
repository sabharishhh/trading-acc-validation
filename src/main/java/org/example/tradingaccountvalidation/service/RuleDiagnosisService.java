package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.ConditionMeta;
import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleDiagnosisInterface;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleDiagnosisService implements RuleDiagnosisInterface {

    @Override
    public List<Map<String, Object>> diagnose(DynamicAccountSnapshot snapshot, List<RuleMeta> rules) {
        List<Map<String, Object>> diagnostics = new ArrayList<>();

        for (RuleMeta rule : rules) {
            List<Map<String, String>> failedConditions = new ArrayList<>();

            for (ConditionMeta condition : rule.getConditions()) {
                String expected = condition.expected();

                if (expected == null || expected.isBlank()) {
                    continue;
                }

                String actual = snapshot.getString(condition.path());

                if (!Objects.equals(expected, actual)) {
                    Map<String, String> fail = new HashMap<>();
                    String safeActual = (actual == null) ? "null" : actual;

                    String template = condition.template();

                    if (template == null || template.isBlank() || template.contains("/account/")) {
                        template = "Expected $expected but found $actual";
                    }
                    else if (!template.contains("$expected") && !template.contains("$actual")) {
                        template = template + " (Expected $expected but found $actual)";
                    }

                    String description = template
                            .replace("$expected", expected)
                            .replace("$actual", safeActual);

                    fail.put("Path", condition.path());
                    fail.put("Reason", description);

                    failedConditions.add(fail);
                }
            }

            if (!failedConditions.isEmpty()) {
                Map<String, Object> ruleDiag = new HashMap<>();
                ruleDiag.put("ruleId", rule.getRuleId());
                ruleDiag.put("failedConditions", failedConditions);
                diagnostics.add(ruleDiag);
            }
        }
        return diagnostics;
    }
}