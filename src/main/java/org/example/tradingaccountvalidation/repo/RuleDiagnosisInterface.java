package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMeta;

import java.util.List;
import java.util.Map;

public interface RuleDiagnosisInterface {
    List<Map<String, Object>> diagnose(
            DynamicAccountSnapshot snapshot,
            List<RuleMeta> rules);
}
