package org.example.tradingaccountvalidation.repo;

import jakarta.annotation.PostConstruct;
import org.example.tradingaccountvalidation.model.RuleMeta;

import java.util.List;

public interface RuleMetadataLoaderInterface {
    @PostConstruct
    void load() throws Exception;

    List<RuleMeta> getByTransition(String from, String to);
}
