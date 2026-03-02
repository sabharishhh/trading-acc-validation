package org.example.tradingaccountvalidation.repo;

import org.kie.api.runtime.KieSession;
import java.io.File;

public interface RuleEngineInterface {

    void reloadRules();
    KieSession newSession();
    void validateRuleFiles(File[] files);
}