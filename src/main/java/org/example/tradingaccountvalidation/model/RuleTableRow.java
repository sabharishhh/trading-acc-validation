package org.example.tradingaccountvalidation.model;

import java.util.Map;

public class RuleTableRow {

    private String ruleId;
    private String statusFrom;
    private String statusTo;
    private String sourceFile;
    private Map<String, String> conditions;

    public RuleTableRow(
            String ruleId,
            String statusFrom,
            String statusTo,
            String sourceFile,
            Map<String, String> conditions
    ) {
        this.ruleId = ruleId;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
        this.sourceFile = sourceFile;
        this.conditions = conditions;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getStatusFrom() {
        return statusFrom;
    }

    public String getStatusTo() {
        return statusTo;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }
}