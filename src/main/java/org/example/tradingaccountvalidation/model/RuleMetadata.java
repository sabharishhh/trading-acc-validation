package org.example.tradingaccountvalidation.model;

import java.util.List;

public class RuleMetadata {

    private String ruleName;
    private String agendaGroup;
    private List<ConditionMetadata> conditions;

    public RuleMetadata(String ruleName,
                        String agendaGroup,
                        List<ConditionMetadata> conditions) {
        this.ruleName = ruleName;
        this.agendaGroup = agendaGroup;
        this.conditions = conditions;
    }

    public String getRuleName() { return ruleName; }
    public String getAgendaGroup() { return agendaGroup; }
    public List<ConditionMetadata> getConditions() { return conditions; }
}
