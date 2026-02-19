package org.example.tradingaccountvalidation.model;

public class ConditionMetadata {

    private String path;
    private String expectedValue;

    public ConditionMetadata(String path, String expectedValue) {
        this.path = path;
        this.expectedValue = expectedValue;
    }

    public String getPath() { return path; }
    public String getExpectedValue() { return expectedValue; }
}