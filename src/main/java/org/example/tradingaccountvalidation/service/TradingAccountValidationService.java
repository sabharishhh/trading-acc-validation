package org.example.tradingaccountvalidation.service;

import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.example.tradingaccountvalidation.repo.TradingAccountValidationInterface;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradingAccountValidationService implements TradingAccountValidationInterface {
    private static final Logger log = LoggerFactory.getLogger(TradingAccountValidationService.class);

    @Autowired
    private KieContainer kieContainer;

    @Override
    public DynamicAccountSnapshot validateAccount(DynamicAccountSnapshot snapshot) {
        log.info("Account object received for id: {}", snapshot.getString("/account/customerId"));

        try {
            KieSession session = kieContainer.newKieSession();

            session.insert(snapshot);
            int firedRule = session.fireAllRules();

            log.info("Rules fired: {}", firedRule);
            return snapshot;

        } catch (Exception e) {
            log.info("Error validating account status for id: {}, error: {}", snapshot.getString("/account/customerId"), e.getMessage());
            return snapshot;
        }
    }
}