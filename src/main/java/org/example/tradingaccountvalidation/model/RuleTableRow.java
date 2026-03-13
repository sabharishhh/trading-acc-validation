package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleTableRow {
    private String ruleId;
    private String agendaGroup;
    private String sourceFile;
    private Map<String,String> conditions;
}