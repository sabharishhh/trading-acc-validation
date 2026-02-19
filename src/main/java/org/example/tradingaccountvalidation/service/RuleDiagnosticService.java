package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.ConditionMetadata;
import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleDiagnosticService {

    @Autowired
    private RuleMetadataLoaderService loader;

    public List<Map<String, Object>> diagnose(
            DynamicAccountSnapshot snapshot,
            String agendaGroup) {

        List<RuleMetadata> rules =
                loader.getRulesForAgenda(agendaGroup);

        List<Map<String, Object>> diagnostics = new ArrayList<>();

        for (RuleMetadata rule : rules) {

            List<Map<String, String>> failedConditions =
                    new ArrayList<>();

            for (ConditionMetadata condition :
                    rule.getConditions()) {

                String actual =
                        snapshot.getString(condition.getPath());

                String expected =
                        condition.getExpectedValue();

                if (!Objects.equals(actual, expected)) {

                    Map<String, String> failure =
                            new HashMap<>();

                    failure.put("path", condition.getPath());
                    failure.put("expected", expected);
                    failure.put("actual", actual);

                    failedConditions.add(failure);
                }
            }

            if (!failedConditions.isEmpty()) {

                Map<String, Object> ruleFailure =
                        new HashMap<>();

                ruleFailure.put("rule", rule.getRuleName());
                ruleFailure.put("failedConditions",
                        failedConditions);

                diagnostics.add(ruleFailure);
            }
        }

        return diagnostics;
    }
}