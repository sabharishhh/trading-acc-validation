package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleDiagnosisInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.repo.TradingAccountValidationInterface;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class TradingAccountValidationService implements TradingAccountValidationInterface {
    private static final Logger log = LoggerFactory.getLogger(TradingAccountValidationService.class);

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private RuleDiagnosisInterface diagnosticService;

    @Autowired
    private RuleMetadataLoaderInterface metadataLoader;

    @Override
    public DynamicAccountSnapshot validateAccount(DynamicAccountSnapshot snapshot) {
        String customerId = snapshot.getString("/account/customerId");

        log.info("Account object received for id: {}", customerId);

        KieSession session = null;

        try {
            session = kieContainer.newKieSession();
            session.insert(snapshot);

            int firedRule = session.fireAllRules();
            log.info("Rules fired: {}", firedRule);

            if (firedRule == 0) {
                String from = snapshot.getString("/account/statusFrom");
                String to = snapshot.getString("/account/statusTo");

                List<RuleMeta> rules = metadataLoader.getByTransition(from, to);
                List<Map<String, Object>> diagnostics = diagnosticService.diagnose(snapshot, rules);

                snapshot.set("/account/output/evaluationStatus", "No Valid Rule Applicable");
                snapshot.set("/account/output/statusFrom", from);
                snapshot.set("/account/output/statusTo", to);
                snapshot.set("/account/output/reasons", diagnostics);
            }
            return snapshot;

        } catch (Exception e) {
            log.error("Validation error for id: {}", customerId, e);

            snapshot.set("/account/output/evaluationStatus", "ERROR");
            snapshot.set("/account/output/message", e.getMessage());

            return snapshot;
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }
}