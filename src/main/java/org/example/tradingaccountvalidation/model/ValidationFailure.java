package org.example.tradingaccountvalidation.model;

public class ValidationFailure {

    private String ruleName;
    private String message;

    public ValidationFailure(String ruleName, String message) {
        this.ruleName = ruleName;
        this.message = message;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getMessage() {
        return message;
    }
}