package org.example.tradingaccountvalidation.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RuleMeta {

    private final String ruleId;
    private final String agendaGroup;
    private final String statusFrom;
    private final String statusTo;
    private final List<ConditionMeta> conditions = new ArrayList<>();

    public RuleMeta(String ruleId,
                    String agendaGroup,
                    String statusFrom,
                    String statusTo) {

        this.ruleId = ruleId;
        this.agendaGroup = agendaGroup;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getAgendaGroup() {
        return agendaGroup;
    }

    public String getStatusFrom() {
        return statusFrom;
    }

    public String getStatusTo() {
        return statusTo;
    }

    public List<ConditionMeta> getConditions() {
        return conditions;
    }
}