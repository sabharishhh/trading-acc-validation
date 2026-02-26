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

                String actual = snapshot.getString(condition.getPath());

                if (!Objects.equals(condition.getExpected(), actual)) {
                    Map<String, String> fail = new HashMap<>();
                    fail.put("path", condition.getPath());
                    fail.put("expected", condition.getExpected());
                    fail.put("actual", actual);

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