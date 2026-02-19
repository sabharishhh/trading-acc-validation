package org.example.tradingaccountvalidation.repo;

import jakarta.annotation.PostConstruct;
import org.example.tradingaccountvalidation.model.RuleMetadata;

import java.util.List;

public interface MetadataLoader {
    @PostConstruct
    void load() throws Exception;

    List<RuleMetadata> getRulesForAgenda(String agenda);
}
