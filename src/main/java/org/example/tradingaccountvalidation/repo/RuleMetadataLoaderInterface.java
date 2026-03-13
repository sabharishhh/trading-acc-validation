package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.model.RuleTableRow;

import java.io.File;
import java.util.List;

public interface RuleMetadataLoaderInterface {
    void reload() throws Exception;
    List<RuleMeta> getAllRules();
    List<RuleTableRow> getRuleTable();
    List<RuleMeta> loadFromFiles(File[] files) throws Exception;
    List<RuleMeta> getByAgendaGroup(String agenda);
}