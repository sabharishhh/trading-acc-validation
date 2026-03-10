package org.example.tradingaccountvalidation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class RuleMeta {
    private String ruleId;
    private String ruleTableName;
    private String agendaGroup;
    private String statusFrom;
    private String statusTo;
    private String sourceFile;
    private final List<ConditionMeta> conditions = new ArrayList<>();

    public RuleMeta(
            String ruleId,
            String ruleTableName,
            String agendaGroup,
            String statusFrom,
            String statusTo,
            String sourceFile
    ) {
        this.ruleId = ruleId;
        this.ruleTableName = ruleTableName;
        this.agendaGroup = agendaGroup;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
        this.sourceFile = sourceFile;
    }
}