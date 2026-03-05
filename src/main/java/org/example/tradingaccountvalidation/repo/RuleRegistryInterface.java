package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.FileInfo;
import org.example.tradingaccountvalidation.model.RuleMeta;

import java.util.List;
import java.util.Map;

public interface RuleRegistryInterface {
    void refresh(String rulesFolderPath,
                                     List<RuleMeta> allRules,
                                     boolean buildStatus);

    List<FileInfo> getFiles();

    Map<String, Object> getStats();
}