package org.example.tradingaccountvalidation.model;

public record ConditionMeta(
        String path,
        String expected,
        String template
) {
    public String getFormattedCondition() {
        if (template == null || template.isBlank()) {
            return path + " = " + expected;
        }
        return template.replace("{value}", expected);
    }
}