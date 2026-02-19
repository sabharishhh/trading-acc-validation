import org.example.tradingaccountvalidation.model.DynamicAccountSnapshot;
import org.kie.api.runtime.KieSession;

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