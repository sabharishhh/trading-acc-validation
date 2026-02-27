package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleDiagnosisInterface;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.repo.TradingAccountValidationInterface;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TradingAccountValidationService implements TradingAccountValidationInterface {
    private final RuleEngineInterface engine;
    private final RuleDiagnosisInterface diagnosis;
    private final RuleMetadataLoaderInterface metadataLoader;

    public TradingAccountValidationService(
            RuleEngineInterface engine,
            RuleDiagnosisInterface diagnosis,
            RuleMetadataLoaderInterface metadataLoader) {

        this.engine = engine;
        this.diagnosis = diagnosis;
        this.metadataLoader = metadataLoader;
    }

    @Override
    public DynamicAccountSnapshot validateAccount(DynamicAccountSnapshot snapshot) {
        KieSession session = null;

        try {
            session = engine.newSession();
            session.insert(snapshot);

            int fired = session.fireAllRules();

            if (fired == 0) {
                String from = snapshot.getString("/account/statusFrom");
                String to = snapshot.getString("/account/statusTo");

                List<RuleMeta> rules = metadataLoader.getByTransition(from, to);
                List<Map<String, Object>> reasons = diagnosis.diagnose(snapshot, rules);

                snapshot.set("/account/output/evaluationStatus", "No Valid Rule Applicable");
                snapshot.set("/account/output/reasons", reasons);
            }
            return snapshot;
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }
}