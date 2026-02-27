package org.example.tradingaccountvalidation.repo;

import org.kie.api.runtime.KieSession;

public interface RuleEngineInterface {

    void reloadRules();
    KieSession newSession();
}