package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.RuleMeta;

import java.util.List;
import java.util.Set;

public interface RuleValidationInterface {
    void validateDuplicateRuleTables(List<RuleMeta> rules, Set<String> uploadingFiles);
}
