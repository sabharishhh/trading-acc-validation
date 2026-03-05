package org.example.tradingaccountvalidation.model;

import java.util.ArrayList;
import java.util.List;

public class RuleMeta {

    private String ruleId;
    private String agendaGroup;
    private String statusFrom;
    private String statusTo;
    private String sourceFile;

    private final List<ConditionMeta> conditions = new ArrayList<>();

    public RuleMeta(String ruleId, String agendaGroup, String statusFrom, String statusTo, String sourceFile) {
        this.ruleId = ruleId;
        this.agendaGroup = agendaGroup;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
        this.sourceFile = sourceFile;
    }

    public String getRuleId() { return ruleId; }
    public String getAgendaGroup() { return agendaGroup; }
    public String getStatusFrom() { return statusFrom; }
    public String getStatusTo() { return statusTo; }
    public String getSourceFile() { return sourceFile; }

    public List<ConditionMeta> getConditions() {
        return conditions;
    }

}