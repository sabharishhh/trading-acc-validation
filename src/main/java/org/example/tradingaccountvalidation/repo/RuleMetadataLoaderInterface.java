package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.model.RuleTableRow;

import java.util.List;

public interface RuleMetadataLoaderInterface {
    void reload() throws Exception;
    List<RuleMeta> getByTransition(String from, String to);
    List<RuleMeta> getAllRules();
    List<RuleTableRow> getRuleTable();
}