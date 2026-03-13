package org.example.tradingaccountvalidation.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleDiagnosisInterface;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.repo.TradingAccountValidationInterface;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaGroup;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Service
public class TradingAccountValidationService implements TradingAccountValidationInterface {

    private final RuleEngineInterface engine;
    private final RuleDiagnosisInterface diagnosis;
    private final RuleMetadataLoaderInterface metadataLoader;

    private static final Logger log = LoggerFactory.getLogger(TradingAccountValidationService.class);

    @Override
    public DynamicAccountSnapshot validateAccount(DynamicAccountSnapshot snapshot) {

        KieSession session = null;

        try {

            session = engine.newSession();

            log.info("--- Inspecting Loaded Knowledge Base ---");

            for (org.kie.api.definition.KiePackage pkg : session.getKieBase().getKiePackages()) {
                for (Rule rule : pkg.getRules()) {

                    Object agendaMeta = rule.getMetaData().get("agenda-group");
                    String group = (agendaMeta != null) ? agendaMeta.toString() : "MAIN";

                    log.info("Rule: [{}] | Group: [{}]", rule.getName(), group);
                }
            }

            session.insert(snapshot);

            // Extract agenda from payload
            String agenda = snapshot.getString("agenda-group");

            if (agenda != null) {
                agenda = agenda.trim();
            }

            if (agenda == null || agenda.isBlank()) {
                throw new RuntimeException("Payload missing 'agenda-group' at root");
            }

            log.info("Focusing on agenda-group: {}", agenda);

            AgendaGroup agendaGroup = session.getAgenda().getAgendaGroup(agenda);

            if (agendaGroup == null) {
                log.error("Agenda group not found: {}", agenda);
                throw new RuntimeException("Agenda group not found: " + agenda);
            }

            agendaGroup.setFocus();

            int fired = session.fireAllRules();

            log.info("Rules fired for [{}]: {}", agenda, fired);

            // Diagnosis fallback
            if (fired == 0) {

                List<RuleMeta> rules = metadataLoader.getByAgendaGroup(agenda);

                log.info("Metadata rules found for agenda [{}]: {}", agenda, rules.size());

                if (!rules.isEmpty()) {

                    List<Map<String, Object>> reasons = diagnosis.diagnose(snapshot, rules);

                    snapshot.set("/account/output/evaluationStatus", "No Valid Rule Applicable");
                    snapshot.set("/account/output/reasons", reasons);

                } else {

                    log.warn("No metadata rules found for agenda {}", agenda);

                    snapshot.set("/account/output/evaluationStatus", "No Valid Rule Applicable");
                    snapshot.set("/account/output/reasons", List.of());
                }
            }

            return snapshot;

        } catch (Exception e) {

            log.error("SYSTEM FAILURE: {}", e.getMessage());
            throw e;

        } finally {

            if (session != null) {
                session.dispose();
            }
        }
    }
}